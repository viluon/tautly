import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG

class Canvas private constructor(
    private val nvgContext: Long,
    private val colourBuffer: NVGColor
) {
    companion object {
        infix fun `in`(nvgContext: Long) = Canvas(nvgContext, NVGColor.malloc())

        fun unsigned_char(x: Float): Byte {
            val int = (255 * x).toInt()
            return if (int > 127) (int - 256).toByte()
            else int.toByte()
        }
    }

    fun circle(cx: Float, cy: Float, r: Float, h: Float, s: Float, l: Float, a: Float) {
        NanoVG.nvgBeginPath(nvgContext)
        NanoVG.nvgCircle(nvgContext, cx, cy, r)
        NanoVG.nvgFillColor(nvgContext, NanoVG.nvgHSLA(h, s, l, unsigned_char(a), colourBuffer))
        NanoVG.nvgFill(nvgContext)
    }

    fun free() {
        colourBuffer.free()
    }
}
