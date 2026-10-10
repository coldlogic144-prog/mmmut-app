package com.mmmut.ero.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/* ==========================================================================
 * MMMUT ERP — shape scale
 * --------------------------------------------------------------------------
 * Deliberately restrained. The brief asks for a professional university ERP,
 * not a consumer app: generous 28dp corners and floating rounded cards read as
 * "template", and they also waste horizontal space on a phone. Corners here
 * stay tight so cards read as structured data panels.
 * ========================================================================== */

val MmmutShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)
