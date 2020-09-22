import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG

class Canvas private constructor(
    private val nvgContext: Long,
    private val colourBuffer: NVGColor
) {
    companion object {
        infix fun `in`(nvgContext: Long) = Canvas(nvgContext, NVGColor.malloc())
    }

    fun circle(cx: Float, cy: Float, rad: Float, r: Byte, g: Byte, b: Byte, a: Byte) {
        NanoVG.nvgBeginPath(nvgContext)
        NanoVG.nvgCircle(nvgContext, cx, cy, rad)
        NanoVG.nvgFillColor(nvgContext, NanoVG.nvgRGBA(r, g, b, a, colourBuffer))
        NanoVG.nvgFill(nvgContext)
    }

    fun free() {
        colourBuffer.free()
    }
}
