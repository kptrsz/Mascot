package com.kptrsz.mascot.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.kptrsz.mascot.core.MascotColor
import com.kptrsz.mascot.core.MascotPalette

public fun Color.toMascotColor(): MascotColor = MascotColor(toArgb())

public fun MascotColor.toComposeColor(): Color = Color(argb)

/** Builds a [MascotPalette] from Compose colors, e.g. from the app's MaterialTheme. */
public fun MascotPalette(body: Color, highlight: Color, glow: Color): MascotPalette =
    MascotPalette(body.toMascotColor(), highlight.toMascotColor(), glow.toMascotColor())
