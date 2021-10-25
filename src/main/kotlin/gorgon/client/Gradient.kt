package gorgon.client

import kotlin.math.roundToInt

data class Color(val r: Double, val g: Double, val b: Double) {
    private fun scaleTo255(x: Double): Int {
        val raw = (x * 255).roundToInt()
        return maxOf(minOf(raw, 255), 0)  // force clamp to 0..255
    }

    fun toHtmlRgbString(): String {
        val rInt = scaleTo255(r)
        val gInt = scaleTo255(g)
        val bInt = scaleTo255(b)
        return "#%02x%02x%02x".format(rInt, gInt, bInt)
    }
}

data class BreakPoint(val value: Double, val color: Color)

class ColorGradient(private val breakPoints: List<BreakPoint>) {
    private fun blend(x: Double, low: Color, high: Color): Color {
        return if (x < 0) {
            low
        } else if (x > 1) {
            high
        } else {
            val r = low.r * (1.0 - x) + high.r * x
            val g = low.g * (1.0 - x) + high.g * x
            val b = low.b * (1.0 - x) + high.b * x
            Color(r, g, b)
        }
    }

    fun toColor(x: Double): Color {
        // before first breakpoint
        if (x <= breakPoints.first().value) {
            return breakPoints.first().color
        }
        for (i in 0 until breakPoints.size - 1) {
            if (breakPoints[i].value <= x && x <= breakPoints[i + 1].value) {
                // found in the middle
                val lambda = (x - breakPoints[i].value) / (breakPoints[i + 1].value - breakPoints[i].value)
                return blend(lambda, breakPoints[i].color, breakPoints[i + 1].color)
            }
        }
        // after last breakpoint
        return breakPoints.last().color
    }
}