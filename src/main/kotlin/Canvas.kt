import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG

class Canvas private constructor(
    private val nvgContext: Long,
    private val colourBuffer: NVGColor,
) {
    companion object {
        infix fun `in`(nvgContext: Long) = Canvas(nvgContext, NVGColor.malloc())

        fun unsigned_char(x: Float): Byte {
            val int = (255 * x).toInt()
            return if (int > 127) (int - 256).toByte()
            else int.toByte()
        }
    }

    fun circle(pos: ScreenSpace, r: Double, h: Double, s: Double, l: Double, a: Double) = fill {
        val (cx, cy) = pos.floats
        NanoVG.nvgCircle(nvgContext, cx, cy, r.toFloat())
        NanoVG.nvgFillColor(nvgContext,
            NanoVG.nvgHSLA(h.toFloat(), s.toFloat(), l.toFloat(), unsigned_char(a.toFloat()), colourBuffer))
    }

    private inline val ScreenSpace.floats: Pair<Float, Float>
        inline get() {
            val (x, y) = this
            return x.toFloat() to y.toFloat()
        }

    fun print(pos: ScreenSpace, s: String) {
        val (x, y) = pos.floats
        NanoVG.nvgFillColor(nvgContext, NanoVG.nvgHSL(0f, 0f, 1f, colourBuffer))
        NanoVG.nvgText(nvgContext, x, y, s)
    }

    fun arrow(origin: ScreenSpace, arrow: ScreenSpace, colour: Triple<Double, Double, Double>) {
        NanoVG.nvgBeginPath(nvgContext)
        NanoVG.nvgStrokeColor(
            nvgContext,
            NanoVG.nvgHSL(colour.first.toFloat(), colour.second.toFloat(), colour.third.toFloat(), colourBuffer)
        )
        val (x0, y0) = origin.floats
        NanoVG.nvgMoveTo(nvgContext, x0, y0)
        val (x1, y1) = arrow.floats
        NanoVG.nvgLineTo(nvgContext, x1, y1)
        NanoVG.nvgStroke(nvgContext)
    }

    private inline fun fill(f: () -> Unit) {
        NanoVG.nvgBeginPath(nvgContext)
        f()
        NanoVG.nvgFill(nvgContext)
    }

    fun free() {
        colourBuffer.free()
    }
}
