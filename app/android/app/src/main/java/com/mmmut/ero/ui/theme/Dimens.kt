package com.mmmut.ero.ui.theme

import androidx.compose.ui.unit.dp

/* ==========================================================================
 * MMMUT ERP — dimensions
 * --------------------------------------------------------------------------
 * One spacing scale, referenced by every screen. Previously screens hardcoded
 * a mix of 4/6/8/10/12/14/16/18/24dp paddings, which is why card padding and
 * gutters did not line up between screens.
 *
 * Everything is a multiple of 4dp so layouts stay on a predictable grid.
 * ========================================================================== */

object Spacing {
    val none = 0.dp
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    /** Standard screen gutter — use for the outer LazyColumn contentPadding. */
    val screen = 16.dp

    /** Padding inside a card. */
    val card = 16.dp

    /** Vertical gap between stacked cards in a list. */
    val listGap = 10.dp
}

object MmmutDimens {
    /** Material's minimum accessible touch target. */
    val touchTarget = 48.dp

    /** Slightly smaller but still generous for dense list rows. */
    val minRowHeight = 56.dp

    val iconSm = 16.dp
    val iconMd = 20.dp
    val iconLg = 24.dp

    /** Left rail on timetable/attendance rows that carries the time. */
    val timeRailWidth = 64.dp

    /** Progress bar used in attendance rows. */
    val barHeight = 6.dp

    /** Avatar / monogram circle in the profile header. */
    val avatar = 56.dp
}
