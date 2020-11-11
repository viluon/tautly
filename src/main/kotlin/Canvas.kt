import Colour.Companion.white
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

    private fun Colour.nvg(): NVGColor = NanoVG.nvgHSLA(
        hue.toFloat(), saturation.toFloat(), lightness.toFloat(), unsigned_char(alpha.toFloat()), colourBuffer
    )

    fun circle(pos: Vec2<Screen>, r: Double, colour: Colour) = fill {
        val (cx, cy) = pos.floats
        NanoVG.nvgCircle(nvgContext, cx, cy, r.toFloat())
        NanoVG.nvgFillColor(nvgContext, colour.nvg())
    }

    fun circleOutline(pos: Vec2<Screen>, r: Double, width: Double, colour: Colour) = stroke {
        val (cx, cy) = pos.floats
        NanoVG.nvgCircle(nvgContext, cx, cy, r.toFloat())
        NanoVG.nvgStrokeColor(nvgContext, colour.nvg())
        NanoVG.nvgStrokeWidth(nvgContext, width.toFloat())
    }

    private inline val Vec2<Screen>.floats: Pair<Float, Float>
        inline get() = x.toFloat() to y.toFloat()

    fun print(pos: Vec2<Screen>, s: String, colour: Colour = white) {
        val (x, y) = pos.floats
        NanoVG.nvgFillColor(nvgContext, colour.nvg())
        NanoVG.nvgText(nvgContext, x, y, s)
    }

    fun rectangle(origin: Vec2<Screen>, size: Vec2<Screen>, colour: Colour) = rect(::fill, origin, size, colour)
    fun rectangleOutline(origin: Vec2<Screen>, size: Vec2<Screen>, colour: Colour) =
        rect(::stroke, origin, size, colour)

    private fun rect(f: (() -> Unit) -> Unit, origin: Vec2<Screen>, size: Vec2<Screen>, colour: Colour) = f {
        val (x, y) = origin.floats
        val (w, h) = size.floats
        NanoVG.nvgRect(nvgContext, x, y, w, h)
        NanoVG.nvgStrokeColor(nvgContext, colour.nvg())
        NanoVG.nvgStrokeWidth(nvgContext, 2.0f)
        NanoVG.nvgFillColor(nvgContext, colour.nvg())
    }

    fun arrow(origin: Vec2<Screen>, arrow: Vec2<Screen>, colour: Colour) {
        NanoVG.nvgBeginPath(nvgContext)
        NanoVG.nvgStrokeColor(nvgContext, colour.nvg())
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

    private inline fun stroke(f: () -> Unit) {
        NanoVG.nvgBeginPath(nvgContext)
        f()
        NanoVG.nvgStroke(nvgContext)
    }

    fun free() {
        colourBuffer.free()
    }
}
