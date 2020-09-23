import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun view(canvas: Canvas, model: Model) {
    model.circles.forEach { (r, pos, colour) ->
        val (h, s, l) = colour
        canvas.circle(model.toScreenSpace(pos), r, h, s, l, 1.0)
    }

    canvas drawPalette model

    val (x, y) = model.offset
    canvas.print(ScreenSpace(model.windowSize.first - 150.0 to 50.0), "(${x.roundToInt()}, ${y.roundToInt()})")
    canvas.print(ScreenSpace(model.windowSize.first - 150.0 to 80.0), "zoom ${model.zoom}")
}

private infix fun Canvas.drawPalette(model: Model) {
    val (ww, wh) = model.windowSize
    val (radius, _, entries) = model.palette

    entries.forEach { (_, pos, colour) ->
        // distance at which the colours disappear completely
        val falloff = min(ww, wh) / 3
        val distZeroOne = min(1.0, max(0.0, (model.cursorPos - pos).magnitude / falloff))
        val alpha = 1 - distZeroOne

        val (h, s, l) = colour
        circle(pos, radius, h, s, l, alpha)
    }
}