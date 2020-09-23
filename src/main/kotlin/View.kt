import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun view(canvas: Canvas, model: Model) {
    model.circles.forEach { (r, pos, colour) ->
        val (x, y) = model.zoom * pos + model.offset
        val (h, s, l) = colour
        canvas.circle(x, y, r, h, s, l, 1f)
    }

    canvas drawPalette model

    canvas.print(model.windowSize.first - 100f, 50f, model.offset.toString())
}

private infix fun Canvas.drawPalette(model: Model) {
    val (ww, wh) = model.windowSize
    val (radius, _, entries) = model.palette

    entries.forEach { (_, pos, colour) ->
        val (h, s, l) = colour
        val (curX, curY) = model.cursorPos
        val (x, y) = pos

        val dist = sqrt((curX - x).pow(2) + (curY - y).pow(2))

        // distance at which the colours disappear completely
        val falloff = min(ww, wh) / 3
        val distZeroOne = min(1.0, max(0.0, dist / falloff))
        val alpha = 1 - distZeroOne

        circle(x, y, radius, h, s, l, alpha.toFloat())
    }
}