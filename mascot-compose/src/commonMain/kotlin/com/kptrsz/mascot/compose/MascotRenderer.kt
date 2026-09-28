package com.kptrsz.mascot.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import com.kptrsz.mascot.core.MascotFrame

/**
 * Draws a [MascotFrame] into a [DrawScope]. Holds a reusable [Path] so a frame allocates
 * nothing but the gradient brushes.
 */
internal class MascotRenderer {
    private val bodyPath = Path()

    fun DrawScope.draw(frame: MascotFrame, showGroundShadow: Boolean) {
        val radius = size.minDimension * BODY_RADIUS_FRACTION
        val groundX = size.width / 2f
        val groundY = size.height * GROUND_Y_FRACTION
        val palette = frame.palette
        val liftPx = frame.lift * radius

        if (showGroundShadow) drawGroundShadow(groundX, groundY, radius, frame.lift)
        drawGlow(
            center = Offset(groundX, groundY - liftPx - radius * frame.scaleY),
            radius = radius * GLOW_RADIUS_FACTOR,
            color = palette.glow.toComposeColor(),
            intensity = frame.glow,
        )

        buildSmoothPath(frame)
        withTransform({
            translate(left = groundX, top = groundY - liftPx)
            // Pivot at the ground contact point so squash/sway feel anchored.
            rotate(frame.rotationDegrees, pivot = Offset.Zero)
            scale(frame.scaleX * radius, frame.scaleY * radius, pivot = Offset.Zero)
        }) {
            drawPath(
                path = bodyPath,
                brush = Brush.verticalGradient(
                    colors = listOf(palette.highlight.toComposeColor(), palette.body.toComposeColor()),
                    startY = -2f,
                    endY = 0f,
                ),
            )
            clipPath(bodyPath) { drawSheen(palette.highlight.toComposeColor()) }
        }
    }

    private fun DrawScope.drawGroundShadow(x: Float, y: Float, radius: Float, lift: Float) {
        val shrink = (1f - lift * 0.5f).coerceIn(0.4f, 1f)
        val width = radius * 1.6f * shrink
        val height = radius * 0.22f * shrink
        drawOval(
            color = Color.Black.copy(alpha = SHADOW_ALPHA * shrink),
            topLeft = Offset(x - width / 2f, y - height / 2f),
            size = Size(width, height),
        )
    }

    private fun DrawScope.drawGlow(center: Offset, radius: Float, color: Color, intensity: Float) {
        if (intensity <= 0f) return
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = GLOW_MAX_ALPHA * intensity), Color.Transparent),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }

    /** Soft specular highlight in unit space; gives volume without implying a face. */
    private fun DrawScope.drawSheen(color: Color) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(-0.35f, -1.5f),
                radius = 0.35f,
            ),
            topLeft = Offset(-0.7f, -1.8f),
            size = Size(0.7f, 0.6f),
        )
    }

    /** Closed Catmull-Rom spline through the outline points, expressed as cubic Béziers. */
    private fun buildSmoothPath(frame: MascotFrame) {
        val points = frame.outline
        val count = frame.pointCount
        fun x(i: Int) = points[2 * ((i + count) % count)]
        fun y(i: Int) = points[2 * ((i + count) % count) + 1]

        bodyPath.reset()
        bodyPath.moveTo(x(0), y(0))
        for (i in 0 until count) {
            val c1x = x(i) + (x(i + 1) - x(i - 1)) / 6f
            val c1y = y(i) + (y(i + 1) - y(i - 1)) / 6f
            val c2x = x(i + 1) - (x(i + 2) - x(i)) / 6f
            val c2y = y(i + 1) - (y(i + 2) - y(i)) / 6f
            bodyPath.cubicTo(c1x, c1y, c2x, c2y, x(i + 1), y(i + 1))
        }
        bodyPath.close()
    }

    private companion object {
        // Leaves headroom for the highest hop, sway and glow within the canvas bounds.
        const val BODY_RADIUS_FRACTION = 0.28f
        const val GROUND_Y_FRACTION = 0.88f
        const val GLOW_RADIUS_FACTOR = 1.7f
        const val GLOW_MAX_ALPHA = 0.55f
        const val SHADOW_ALPHA = 0.16f
    }
}
