/* ==========================================================================
   MMMUT Holographic Campus — WebGL scene
   --------------------------------------------------------------------------
   A self-contained 3D frontend for the MMMUT ERO Android app.

   Design constraints that shaped this file:
     * No dependencies and no network access — the app is used on campus wifi
       and offline, so everything is hand-written raw WebGL. Nothing is fetched.
     * Must survive low-end devices: one mesh program (+ a flat-shading mode
       used for grid lines) and one point program, no post-processing, no
       framebuffers, devicePixelRatio capped at 2.
     * Degrades honestly: if WebGL is unavailable we hand control to the CSS
       fallback rather than showing a blank canvas.

   Public bridge used by the Android WebView host:
     window.MMMUT3D.setStats({ attendance, term, next, nextHint })
     window.MMMUT3D.focus('academic' | 'hostel' | 'library' | 'sports' | 'all')
   ========================================================================== */

(function () {
  'use strict';

  /* ------------------------------------------------------------------ DOM */

  var canvas   = document.getElementById('gl');
  var fallback = document.getElementById('fallback');

  var elAttendance    = document.getElementById('statAttendance');
  var elAttendanceBar = document.getElementById('statAttendanceBar');
  var elTerm          = document.getElementById('statTerm');
  var elTermHint      = document.getElementById('statTermHint');
  var elNext          = document.getElementById('statNext');
  var elNextHint      = document.getElementById('statNextHint');
  var elSubtitle      = document.getElementById('hudSubtitle');

  /* -------------------------------------------------------------- GL init */

  var gl = null;
  try {
    var attrs = { antialias: true, alpha: false, depth: true, powerPreference: 'high-performance' };
    gl = canvas.getContext('webgl', attrs) || canvas.getContext('experimental-webgl', attrs);
  } catch (e) {
    gl = null;
  }

  if (!gl) { showFallback(); return; }

  function showFallback() {
    if (canvas) { canvas.style.display = 'none'; }
    if (fallback) { fallback.hidden = false; }
  }

  /* ================================================================ math ==
     Minimal column-major 4x4 / 3x3 helpers. Only what the scene needs.      */

  function perspective(out, fovy, aspect, near, far) {
    var f = 1 / Math.tan(fovy / 2), nf = 1 / (near - far);
    out[0] = f / aspect; out[1] = 0; out[2] = 0;               out[3] = 0;
    out[4] = 0;          out[5] = f; out[6] = 0;               out[7] = 0;
    out[8] = 0;          out[9] = 0; out[10] = (far + near) * nf; out[11] = -1;
    out[12] = 0;         out[13] = 0; out[14] = 2 * far * near * nf; out[15] = 0;
    return out;
  }

  function lookAt(out, eye, center, up) {
    var z0 = eye[0] - center[0], z1 = eye[1] - center[1], z2 = eye[2] - center[2];
    var len = Math.sqrt(z0 * z0 + z1 * z1 + z2 * z2) || 1;
    z0 /= len; z1 /= len; z2 /= len;

    var x0 = up[1] * z2 - up[2] * z1,
        x1 = up[2] * z0 - up[0] * z2,
        x2 = up[0] * z1 - up[1] * z0;
    len = Math.sqrt(x0 * x0 + x1 * x1 + x2 * x2);
    if (len) { x0 /= len; x1 /= len; x2 /= len; } else { x0 = 1; x1 = 0; x2 = 0; }

    var y0 = z1 * x2 - z2 * x1,
        y1 = z2 * x0 - z0 * x2,
        y2 = z0 * x1 - z1 * x0;

    out[0] = x0; out[1] = y0; out[2]  = z0; out[3]  = 0;
    out[4] = x1; out[5] = y1; out[6]  = z1; out[7]  = 0;
    out[8] = x2; out[9] = y2; out[10] = z2; out[11] = 0;
    out[12] = -(x0 * eye[0] + x1 * eye[1] + x2 * eye[2]);
    out[13] = -(y0 * eye[0] + y1 * eye[1] + y2 * eye[2]);
    out[14] = -(z0 * eye[0] + z1 * eye[1] + z2 * eye[2]);
    out[15] = 1;
    return out;
  }

  /**
   * Build a rotation (Ry * Rx) + scale + translation matrix, and the matching
   * rotation-only 3x3 for normals. Deriving both in one place keeps them in
   * sync — a mismatched normal matrix is the classic cause of lighting that
   * looks "almost right" but subtly wrong.
   */
  function setModel(m, n3, tx, ty, tz, rx, ry, sx, sy, sz) {
    var cx = Math.cos(rx), sxr = Math.sin(rx);
    var cy = Math.cos(ry), syr = Math.sin(ry);

    var r00 = cy,  r01 = syr * sxr, r02 = syr * cx;
    var r10 = 0,   r11 = cx,        r12 = -sxr;
    var r20 = -syr, r21 = cy * sxr, r22 = cy * cx;

    m[0] = r00 * sx; m[1]  = r10 * sx; m[2]  = r20 * sx; m[3]  = 0;
    m[4] = r01 * sy; m[5]  = r11 * sy; m[6]  = r21 * sy; m[7]  = 0;
    m[8] = r02 * sz; m[9]  = r12 * sz; m[10] = r22 * sz; m[11] = 0;
    m[12] = tx;      m[13] = ty;       m[14] = tz;       m[15] = 1;

    n3[0] = r00; n3[1] = r10; n3[2] = r20;
    n3[3] = r01; n3[4] = r11; n3[5] = r21;
    n3[6] = r02; n3[7] = r12; n3[8] = r22;
    return m;
  }

  /* ============================================================ shaders == */

  var MESH_VS = [
    'attribute vec3 aPos;',
    'attribute vec3 aNormal;',
    'uniform mat4 uProj, uView, uModel;',
    'uniform mat3 uNormal;',
    'varying vec3 vN;',
    'varying vec3 vW;',
    'void main() {',
    '  vec4 world = uModel * vec4(aPos, 1.0);',
    '  vW = world.xyz;',
    '  vN = uNormal * aNormal;',
    '  gl_Position = uProj * uView * world;',
    '}'
  ].join('\n');

  var MESH_FS = [
    'precision mediump float;',
    'varying vec3 vN;',
    'varying vec3 vW;',
    'uniform vec3 uColor;',
    'uniform vec3 uEye;',
    'uniform vec3 uLight;',
    'uniform float uAlpha;',
    'uniform float uFlat;',
    'uniform float uEmissive;',
    'void main() {',
    // uFlat === 1 -> unlit line/grid rendering, skip the lighting maths.
    '  if (uFlat > 0.5) { gl_FragColor = vec4(uColor, uAlpha); return; }',
    '  vec3 n = normalize(vN);',
    '  vec3 L = normalize(uLight);',
    '  vec3 V = normalize(uEye - vW);',
    '  float diff = max(dot(n, L), 0.0);',
    '  float rim  = pow(1.0 - max(dot(n, V), 0.0), 2.2);',
    '  vec3  H    = normalize(L + V);',
    '  float spec = pow(max(dot(n, H), 0.0), 42.0) * 0.5;',
    '  vec3 col = uColor * (0.24 + 0.76 * diff)',
    '           + vec3(0.35, 0.55, 1.0) * rim * 0.85',
    '           + vec3(spec)',
    '           + uColor * uEmissive;',
    '  gl_FragColor = vec4(col, uAlpha);',
    '}'
  ].join('\n');

  var POINT_VS = [
    'attribute vec3 aPos;',
    'attribute float aSize;',
    'uniform mat4 uProj, uView;',
    'uniform float uPixelScale;',
    'varying float vFade;',
    'void main() {',
    '  vec4 mv = uView * vec4(aPos, 1.0);',
    '  gl_Position = uProj * mv;',
    '  float depth = max(-mv.z, 0.001);',
    '  gl_PointSize = clamp(aSize * uPixelScale / depth, 1.0, 14.0);',
    // Fade with distance so the field dissolves into the background instead
    // of ending at a visible wall.
    '  vFade = clamp(1.0 - depth / 70.0, 0.0, 1.0);',
    '}'
  ].join('\n');

  var POINT_FS = [
    'precision mediump float;',
    'varying float vFade;',
    'uniform vec3 uColor;',
    'void main() {',
    '  vec2 d = gl_PointCoord - vec2(0.5);',
    '  float r = length(d);',
    '  if (r > 0.5) discard;',
    '  float a = smoothstep(0.5, 0.0, r) * vFade;',
    '  gl_FragColor = vec4(uColor, a * 0.9);',
    '}'
  ].join('\n');

  function compile(type, src) {
    var s = gl.createShader(type);
    gl.shaderSource(s, src);
    gl.compileShader(s);
    if (!gl.getShaderParameter(s, gl.COMPILE_STATUS)) {
      throw new Error('shader: ' + gl.getShaderInfoLog(s));
    }
    return s;
  }

  function program(vsSrc, fsSrc, attribs, uniforms) {
    var p = gl.createProgram();
    gl.attachShader(p, compile(gl.VERTEX_SHADER, vsSrc));
    gl.attachShader(p, compile(gl.FRAGMENT_SHADER, fsSrc));
    gl.linkProgram(p);
    if (!gl.getProgramParameter(p, gl.LINK_STATUS)) {
      throw new Error('program: ' + gl.getProgramInfoLog(p));
    }
    var o = { program: p, a: {}, u: {} };
    attribs.forEach(function (n) { o.a[n] = gl.getAttribLocation(p, n); });
    uniforms.forEach(function (n) { o.u[n] = gl.getUniformLocation(p, n); });
    return o;
  }

  var meshP, pointP;
  try {
    meshP = program(MESH_VS, MESH_FS,
      ['aPos', 'aNormal'],
      ['uProj', 'uView', 'uModel', 'uNormal', 'uColor', 'uEye', 'uLight', 'uAlpha', 'uFlat', 'uEmissive']);
    pointP = program(POINT_VS, POINT_FS,
      ['aPos', 'aSize'],
      ['uProj', 'uView', 'uPixelScale', 'uColor']);
  } catch (err) {
    showFallback();
    return;
  }

  /* ============================================================ geometry == */

  /** Unit cube spanning -1..1 on every axis, 24 verts so faces have flat normals. */
  function buildCube() {
    var faces = [
      { n: [0, 0, 1],  v: [[-1, -1, 1], [1, -1, 1], [1, 1, 1], [-1, 1, 1]] },
      { n: [0, 0, -1], v: [[1, -1, -1], [-1, -1, -1], [-1, 1, -1], [1, 1, -1]] },
      { n: [0, 1, 0],  v: [[-1, 1, 1], [1, 1, 1], [1, 1, -1], [-1, 1, -1]] },
      { n: [0, -1, 0], v: [[-1, -1, -1], [1, -1, -1], [1, -1, 1], [-1, -1, 1]] },
      { n: [1, 0, 0],  v: [[1, -1, 1], [1, -1, -1], [1, 1, -1], [1, 1, 1]] },
      { n: [-1, 0, 0], v: [[-1, -1, -1], [-1, -1, 1], [-1, 1, 1], [-1, 1, -1]] }
    ];
    var data = [], idx = [];
    faces.forEach(function (f, fi) {
      f.v.forEach(function (v) {
        data.push(v[0], v[1], v[2], f.n[0], f.n[1], f.n[2]);
      });
      var b = fi * 4;
      idx.push(b, b + 1, b + 2, b, b + 2, b + 3);
    });
    return { data: new Float32Array(data), idx: new Uint16Array(idx) };
  }

  /** Octahedron, non-indexed so each face gets an exact flat normal. */
  function buildOctahedron() {
    var P = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]];
    var tris = [
      [0, 2, 4], [2, 1, 4], [1, 3, 4], [3, 0, 4],
      [2, 0, 5], [1, 2, 5], [3, 1, 5], [0, 3, 5]
    ];
    var data = [];
    tris.forEach(function (t) {
      var a = P[t[0]], b = P[t[1]], c = P[t[2]];
      var u = [b[0] - a[0], b[1] - a[1], b[2] - a[2]];
      var v = [c[0] - a[0], c[1] - a[1], c[2] - a[2]];
      var nx = u[1] * v[2] - u[2] * v[1];
      var ny = u[2] * v[0] - u[0] * v[2];
      var nz = u[0] * v[1] - u[1] * v[0];
      var l = Math.sqrt(nx * nx + ny * ny + nz * nz) || 1;
      nx /= l; ny /= l; nz /= l;
      [a, b, c].forEach(function (p) { data.push(p[0], p[1], p[2], nx, ny, nz); });
    });
    return { data: new Float32Array(data), count: tris.length * 3 };
  }

  /** Ground grid as line pairs (y = 0). */
  function buildGrid(extent, step) {
    var v = [];
    for (var i = -extent; i <= extent; i += step) {
      v.push(i, 0, -extent, i, 0, extent);
      v.push(-extent, 0, i, extent, 0, i);
    }
    return new Float32Array(v);
  }

  /** Deterministic pseudo-random so the sky and city look identical every launch. */
  function rand(seed) {
    var x = Math.sin(seed * 12.9898) * 43758.5453;
    return x - Math.floor(x);
  }

  /** Star/dust field in a wide dome above and around the campus. */
  function buildParticles(count) {
    var pos = new Float32Array(count * 3);
    var size = new Float32Array(count);
    for (var i = 0; i < count; i++) {
      var a = rand(i + 1) * Math.PI * 2;
      var r = 12 + rand(i + 2) * 46;
      pos[i * 3]     = Math.cos(a) * r;
      pos[i * 3 + 1] = 1 + rand(i + 3) * 34;
      pos[i * 3 + 2] = Math.sin(a) * r;
      size[i] = 0.8 + rand(i + 4) * 2.4;
    }
    return { pos: pos, size: size, count: count };
  }

  function buffer(target, data, usage) {
    var b = gl.createBuffer();
    gl.bindBuffer(target, b);
    gl.bufferData(target, data, usage || gl.STATIC_DRAW);
    return b;
  }

  var cube = buildCube();
  var cubeBuf = buffer(gl.ARRAY_BUFFER, cube.data);
  var cubeIdx = buffer(gl.ELEMENT_ARRAY_BUFFER, cube.idx);

  var octa = buildOctahedron();
  var octaBuf = buffer(gl.ARRAY_BUFFER, octa.data);

  // Derive the vertex count from the data itself. Hardcoding it here is how an
  // off-by-N creeps in and makes the driver read past the buffer.
  var gridData = buildGrid(30, 3);
  var gridBuf = buffer(gl.ARRAY_BUFFER, gridData);
  var gridCount = gridData.length / 3;

  var dust = buildParticles(900);
  var dustPosBuf  = buffer(gl.ARRAY_BUFFER, dust.pos);
  var dustSizeBuf = buffer(gl.ARRAY_BUFFER, dust.size);

  /* ============================================================== campus == */

  var PALETTE = {
    academic: [0.36, 0.44, 0.98],
    hostel:   [0.96, 0.62, 0.32],
    library:  [0.98, 0.78, 0.34],
    sports:   [0.72, 0.22, 0.32],
    admin:    [0.62, 0.72, 1.00],
    core:     [1.00, 0.80, 0.42]
  };

  /**
   * Campus blocks read as a ring of buildings around a central landmark.
   * Heights are hand-tuned per group so each zone is recognisable by silhouette
   * alone — the point of the 3D view is orientation, not realism.
   */
  var buildings = [];
  (function layout() {
    var defs = [
      ['Academic Block A', 'academic', 2.6, 9.0, 2.2, 2.2],
      ['Academic Block B', 'academic', 3.0, 7.2, 2.0, 2.0],
      ['Academic Block C', 'academic', 2.4, 8.4, 2.4, 2.0],
      ['Central Library', 'library', 3.2, 11.5, 2.8, 2.8],
      ['Hostel — Tagore', 'hostel', 2.2, 5.6, 3.4, 1.8],
      ['Hostel — VS', 'hostel', 2.2, 6.2, 3.4, 1.8],
      ['Hostel — Raman', 'hostel', 2.0, 5.0, 3.0, 1.8],
      ['Sports Complex', 'sports', 5.4, 2.4, 3.6, 3.6],
      ['Administration', 'admin', 3.0, 7.6, 3.0, 2.4],
      ['Lecture Hall Complex', 'academic', 3.4, 6.4, 3.2, 2.2],
      ['Workshop', 'academic', 3.0, 3.6, 3.0, 2.6],
      ['Innovation Centre', 'library', 2.4, 7.0, 2.0, 2.0],
      ['Auditorium', 'admin', 4.2, 4.2, 3.6, 3.0],
      ['Guest House', 'hostel', 2.0, 4.0, 2.6, 2.0],
      ['Health Centre', 'admin', 2.0, 3.4, 2.4, 2.0],
      ['Sports Ground', 'sports', 6.0, 1.2, 4.4, 4.4]
    ];

    defs.forEach(function (d, i) {
      var total = defs.length;
      var a = (i / total) * Math.PI * 2;
      var ring = 13 + (i % 2) * 3.5;

      buildings.push({
        name: d[0],
        group: d[1],
        color: PALETTE[d[1]] || PALETTE.admin,
        // x/z, footprint, height
        x: Math.cos(a) * ring,
        z: Math.sin(a) * ring,
        w: d[2],
        h: d[3],
        d: d[4],
        // rotate each block so its face points at the plaza
        ry: -a + Math.PI / 2,
        // per-building phase so the "breathing" pulse is not synchronised
        phase: rand(i + 21) * Math.PI * 2
      });
    });
  })();

  var orbitNodes = [];
  for (var on = 0; on < 6; on++) {
    orbitNodes.push({ a: (on / 6) * Math.PI * 2, r: 5.2, y: 5.4, s: 0.34 });
  }

  /* ============================================================== camera == */

  var cam = {
    yaw: 0.6,
    pitch: 0.42,
    dist: 46,
    targetYaw: 0.6,
    targetPitch: 0.42,
    targetDist: 46,
    tx: 0, ty: 4, tz: 0,
    targetTx: 0, targetTy: 4, targetTz: 0
  };

  var focusMode = 'all';
  var resetBtn = document.getElementById('btnReset');

  /* ========================================================== interaction == */

  var dragging = false, lastX = 0, lastY = 0, moved = 0;
  var pinchDist = 0;

  function pointerPos(e) {
    if (e.touches && e.touches.length) {
      return { x: e.touches[0].clientX, y: e.touches[0].clientY };
    }
    return { x: e.clientX, y: e.clientY };
  }

  function onDown(e) {
    if (e.touches && e.touches.length === 2) {
      pinchDist = touchDistance(e);
      dragging = false;
      return;
    }
    dragging = true;
    moved = 0;
    var p = pointerPos(e);
    lastX = p.x; lastY = p.y;
  }

  function onMove(e) {
    if (e.touches && e.touches.length === 2) {
      var d = touchDistance(e);
      if (pinchDist > 0) {
        cam.targetDist = clamp(cam.targetDist * (pinchDist / d), 16, 92);
      }
      pinchDist = d;
      e.preventDefault();
      return;
    }
    if (!dragging) return;

    var p = pointerPos(e);
    var dx = p.x - lastX, dy = p.y - lastY;
    lastX = p.x; lastY = p.y;
    moved += Math.abs(dx) + Math.abs(dy);

    cam.targetYaw   -= dx * 0.006;
    cam.targetPitch  = clamp(cam.targetPitch + dy * 0.004, 0.06, 1.25);

    if (e.cancelable) e.preventDefault();
  }

  function onUp() { dragging = false; pinchDist = 0; }

  function touchDistance(e) {
    var dx = e.touches[0].clientX - e.touches[1].clientX;
    var dy = e.touches[0].clientY - e.touches[1].clientY;
    return Math.sqrt(dx * dx + dy * dy) || 1;
  }

  function onWheel(e) {
    cam.targetDist = clamp(cam.targetDist + (e.deltaY > 0 ? 3.4 : -3.4), 16, 92);
    e.preventDefault();
  }

  canvas.addEventListener('mousedown', onDown);
  window.addEventListener('mousemove', onMove);
  window.addEventListener('mouseup', onUp);
  canvas.addEventListener('touchstart', onDown, { passive: false });
  canvas.addEventListener('touchmove', onMove, { passive: false });
  canvas.addEventListener('touchend', onUp);
  canvas.addEventListener('wheel', onWheel, { passive: false });
  canvas.addEventListener('dblclick', function () { resetCamera(); });

  if (resetBtn) resetBtn.addEventListener('click', function () { resetCamera(); });

  function resetCamera() {
    cam.targetYaw = 0.6;
    cam.targetPitch = 0.42;
    cam.targetDist = 46;
    cam.targetTx = 0; cam.targetTy = 4; cam.targetTz = 0;
    setFocus('all');
  }

  /* ------------------------------------------------------------- focus ---- */

  function setFocus(mode) {
    focusMode = mode;
    // Frame the zone by aiming the camera at its centroid.
    var group = buildings.filter(function (b) { return mode === 'all' || b.group === mode; });
    if (group.length) {
      var sx = 0, sz = 0, sy = 0;
      group.forEach(function (b) { sx += b.x; sz += b.z; sy += b.h * 0.5; });
      cam.targetTx = sx / group.length;
      cam.targetTz = sz / group.length;
      cam.targetTy = sy / group.length;
      cam.targetDist = mode === 'all' ? 46 : 30;
    }
    var chips = document.querySelectorAll('.chip[data-focus]');
    for (var i = 0; i < chips.length; i++) {
      chips[i].classList.toggle('is-active', chips[i].getAttribute('data-focus') === mode);
    }
  }

  var legend = document.getElementById('hudLegend');
  if (legend) {
    legend.addEventListener('click', function (e) {
      var t = e.target;
      if (!t || !t.getAttribute) return;
      var f = t.getAttribute('data-focus');
      if (f) setFocus(f);
    });
  }

  /* =============================================================== render == */

  var proj = new Float32Array(16);
  var view = new Float32Array(16);
  var model = new Float32Array(16);
  var normal = new Float32Array(9);

  var LIGHT = [0.45, 0.82, 0.36];
  var EYE = new Float32Array(3);

  var dpr = 1;

  function resize() {
    dpr = Math.min(window.devicePixelRatio || 1, 2);
    var w = Math.max(1, Math.floor(canvas.clientWidth * dpr));
    var h = Math.max(1, Math.floor(canvas.clientHeight * dpr));
    if (canvas.width !== w || canvas.height !== h) {
      canvas.width = w;
      canvas.height = h;
    }
  }

  window.addEventListener('resize', resize);
  window.addEventListener('orientationchange', function () { setTimeout(resize, 120); });
  resize();

  gl.enable(gl.DEPTH_TEST);
  gl.enable(gl.CULL_FACE);
  gl.cullFace(gl.BACK);
  gl.clearColor(0.02, 0.03, 0.07, 1);

  function clamp(v, lo, hi) { return v < lo ? lo : (v > hi ? hi : v); }

  function bindMeshAttribs() {
    gl.bindBuffer(gl.ARRAY_BUFFER, cubeBuf);
    gl.enableVertexAttribArray(meshP.a.aPos);
    gl.vertexAttribPointer(meshP.a.aPos, 3, gl.FLOAT, false, 24, 0);
    gl.enableVertexAttribArray(meshP.a.aNormal);
    gl.vertexAttribPointer(meshP.a.aNormal, 3, gl.FLOAT, false, 24, 12);
  }

  function drawCube(tx, ty, tz, rx, ry, sx, sy, sz, color, alpha, emissive) {
    setModel(model, normal, tx, ty, tz, rx, ry, sx, sy, sz);
    gl.uniformMatrix4fv(meshP.u.uModel, false, model);
    gl.uniformMatrix3fv(meshP.u.uNormal, false, normal);
    gl.uniform3fv(meshP.u.uColor, color);
    gl.uniform1f(meshP.u.uAlpha, alpha);
    gl.uniform1f(meshP.u.uFlat, 0);
    gl.uniform1f(meshP.u.uEmissive, emissive || 0);
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, cubeIdx);
    gl.drawElements(gl.TRIANGLES, 36, gl.UNSIGNED_SHORT, 0);
  }

  var start = (window.performance && performance.now) ? performance.now() : Date.now();

  function frame(now) {
    var t = ((now || Date.now()) - start) / 1000;

    resize();

    // --- camera damping -----------------------------------------------------
    cam.yaw   += (cam.targetYaw - cam.yaw) * 0.09;
    cam.pitch += (cam.targetPitch - cam.pitch) * 0.09;
    cam.dist  += (cam.targetDist - cam.dist) * 0.09;
    cam.tx    += (cam.targetTx - cam.tx) * 0.07;
    cam.ty    += (cam.targetTy - cam.ty) * 0.07;
    cam.tz    += (cam.targetTz - cam.tz) * 0.07;

    // Slow idle drift keeps the scene alive when nobody is touching it.
    var idleYaw = cam.yaw + (dragging ? 0 : t * 0.035);

    var cy = Math.cos(cam.pitch);
    EYE[0] = cam.tx + Math.sin(idleYaw) * cam.dist * cy;
    EYE[1] = cam.ty + Math.sin(cam.pitch) * cam.dist;
    EYE[2] = cam.tz + Math.cos(idleYaw) * cam.dist * cy;

    perspective(proj, 52 * Math.PI / 180, canvas.width / canvas.height, 0.5, 260);
    lookAt(view, EYE, [cam.tx, cam.ty, cam.tz], [0, 1, 0]);

    gl.viewport(0, 0, canvas.width, canvas.height);
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

    /* ---------------------------------------------------------- grid ----- */
    gl.useProgram(meshP.program);
    gl.uniformMatrix4fv(meshP.u.uProj, false, proj);
    gl.uniformMatrix4fv(meshP.u.uView, false, view);
    gl.uniform3fv(meshP.u.uEye, EYE);
    gl.uniform3fv(meshP.u.uLight, LIGHT);
    gl.uniform1f(meshP.u.uEmissive, 0);

    // grid needs position only; point aNormal at the same buffer is harmless
    // because uFlat skips lighting entirely.
    gl.bindBuffer(gl.ARRAY_BUFFER, gridBuf);
    gl.enableVertexAttribArray(meshP.a.aPos);
    gl.vertexAttribPointer(meshP.a.aPos, 3, gl.FLOAT, false, 0, 0);
    gl.disableVertexAttribArray(meshP.a.aNormal);
    gl.vertexAttrib3f(meshP.a.aNormal, 0, 1, 0);

    setModel(model, normal, 0, 0, 0, 0, 0, 1, 1, 1);
    gl.uniformMatrix4fv(meshP.u.uModel, false, model);
    gl.uniformMatrix3fv(meshP.u.uNormal, false, normal);
    gl.uniform3f(meshP.u.uColor, 0.22, 0.32, 0.62);
    gl.uniform1f(meshP.u.uAlpha, 0.30);
    gl.uniform1f(meshP.u.uFlat, 1);
    gl.enable(gl.BLEND);
    gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
    gl.depthMask(false);
    gl.drawArrays(gl.LINES, 0, gridCount);
    gl.depthMask(true);

    /* ----------------------------------------------------- buildings ----- */
    bindMeshAttribs();
    gl.uniform3fv(meshP.u.uEye, EYE);
    gl.uniform3fv(meshP.u.uLight, LIGHT);

    for (var i = 0; i < buildings.length; i++) {
      var b = buildings[i];
      var active = (focusMode === 'all' || b.group === focusMode);
      var pulse = 1 + Math.sin(t * 1.1 + b.phase) * 0.02;

      // Dim, don't hide: keeping inactive blocks visible preserves the sense of
      // a real campus around the focused zone.
      var alpha = active ? 1.0 : 0.13;
      var emissive = active ? 0.10 : 0.0;

      drawCube(
        b.x, b.h * 0.5 * pulse, b.z,
        0, b.ry,
        b.w * 0.5, b.h * 0.5 * pulse, b.d * 0.5,
        b.color, alpha, emissive
      );

      // roof accent
      if (active) {
        drawCube(
          b.x, b.h * pulse + 0.16, b.z,
          0, b.ry,
          b.w * 0.42, 0.1, b.d * 0.42,
          PALETTE.core, 0.85, 0.5
        );
      }
    }

    /* ---------------------------------------------------------- plaza ---- */
    gl.disable(gl.BLEND);
    drawCube(0, 0.35, 0, 0, t * 0.05, 7.4, 0.35, 7.4, [0.14, 0.18, 0.34], 1.0, 0.12);
    drawCube(0, 0.72, 0, 0, -t * 0.08, 5.0, 0.12, 5.0, PALETTE.core, 1.0, 0.35);

    /* --------------------------------------------------------- crystal --- */
    var floatY = 4.6 + Math.sin(t * 0.9) * 0.35;
    var spin = t * 0.85;

    gl.bindBuffer(gl.ARRAY_BUFFER, octaBuf);
    gl.enableVertexAttribArray(meshP.a.aPos);
    gl.vertexAttribPointer(meshP.a.aPos, 3, gl.FLOAT, false, 24, 0);
    gl.enableVertexAttribArray(meshP.a.aNormal);
    gl.vertexAttribPointer(meshP.a.aNormal, 3, gl.FLOAT, false, 24, 12);

    setModel(model, normal, 0, floatY, 0, 0.55, spin, 1.9, 1.9, 1.9);
    gl.uniformMatrix4fv(meshP.u.uModel, false, model);
    gl.uniformMatrix3fv(meshP.u.uNormal, false, normal);
    gl.uniform3fv(meshP.u.uColor, PALETTE.core);
    gl.uniform1f(meshP.u.uAlpha, 1.0);
    gl.uniform1f(meshP.u.uFlat, 0);
    gl.uniform1f(meshP.u.uEmissive, 0.42);
    gl.drawArrays(gl.TRIANGLES, 0, octa.count);

    /* ----------------------------------------------------- orbit nodes --- */
    bindMeshAttribs();
    for (var k = 0; k < orbitNodes.length; k++) {
      var nd = orbitNodes[k];
      var ang = nd.a + t * 0.5;
      drawCube(
        Math.cos(ang) * nd.r, nd.y + Math.sin(t * 1.3 + k) * 0.5, Math.sin(ang) * nd.r,
        t * 0.7, ang,
        nd.s, nd.s, nd.s,
        PALETTE.admin, 0.95, 0.55
      );
    }

    /* --------------------------------------------------------- dust ------ */
    gl.useProgram(pointP.program);
    gl.uniformMatrix4fv(pointP.u.uProj, false, proj);
    gl.uniformMatrix4fv(pointP.u.uView, false, view);
    gl.uniform1f(pointP.u.uPixelScale, 26 * dpr);
    gl.uniform3f(pointP.u.uColor, 0.66, 0.78, 1.0);

    gl.bindBuffer(gl.ARRAY_BUFFER, dustPosBuf);
    gl.enableVertexAttribArray(pointP.a.aPos);
    gl.vertexAttribPointer(pointP.a.aPos, 3, gl.FLOAT, false, 0, 0);
    gl.bindBuffer(gl.ARRAY_BUFFER, dustSizeBuf);
    gl.enableVertexAttribArray(pointP.a.aSize);
    gl.vertexAttribPointer(pointP.a.aSize, 1, gl.FLOAT, false, 0, 0);

    gl.enable(gl.BLEND);
    gl.blendFunc(gl.SRC_ALPHA, gl.ONE);   // additive: reads as light, not haze
    gl.depthMask(false);
    gl.drawArrays(gl.POINTS, 0, dust.count);
    gl.depthMask(true);
    gl.disable(gl.BLEND);

    raf = requestAnimationFrame(frame);
  }

  var raf = requestAnimationFrame(frame);

  /* ------------------------------------------------- pause when hidden ---- */
  document.addEventListener('visibilitychange', function () {
    if (document.hidden) {
      if (raf) { cancelAnimationFrame(raf); raf = 0; }
    } else if (!raf) {
      start = ((window.performance && performance.now) ? performance.now() : Date.now());
      raf = requestAnimationFrame(frame);
    }
  });

  /* =============================================================== bridge == */

  function setText(el, value) { if (el && value != null) el.textContent = String(value); }

  function setStats(o) {
    if (!o) return;

    if (o.attendance != null && o.attendance !== '') {
      var n = parseFloat(o.attendance);
      setText(elAttendance, isNaN(n) ? o.attendance : (Math.round(n * 10) / 10) + '%');
      if (elAttendanceBar && !isNaN(n)) {
        elAttendanceBar.style.width = clamp(n, 0, 100) + '%';
      }
    }
    if (o.term) setText(elTerm, o.term);
    if (o.termHint) setText(elTermHint, o.termHint);
    if (o.next) setText(elNext, o.next);
    if (o.nextHint) setText(elNextHint, o.nextHint);
    if (o.subtitle) setText(elSubtitle, o.subtitle);
  }

  window.MMMUT3D = {
    isReady: function () { return true; },
    setStats: setStats,
    focus: setFocus,
    reset: resetCamera,
    buildingCount: buildings.length
  };

  /* Seed from the URL so the Android host can pass state without JS injection:
     campus3d/index.html?attendance=87&next=CS201&term=2026-27                 */
  try {
    var q = new URLSearchParams(window.location.search || '');
    setStats({
      attendance: q.get('attendance'),
      term: q.get('term'),
      termHint: q.get('termHint'),
      next: q.get('next'),
      nextHint: q.get('nextHint'),
      subtitle: q.get('subtitle')
    });
    var f = q.get('focus');
    if (f) setFocus(f);
  } catch (e) { /* URLSearchParams missing on very old WebViews: static HUD is fine */ }

})();
