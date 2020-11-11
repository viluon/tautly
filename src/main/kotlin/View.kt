import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun view(canvas: Canvas, model: Model) {
//    model.circles.forEach { (r, pos, colour) ->
//        canvas.circle(model.toScreenSpace(pos), r, colour)
//    }

    canvas drawWorld model
    canvas drawPalette model

    val (x, y) = model.offset
    canvas.print(Vec2.screen(model.windowSize.x - 150.0, 50.0), "(${x.roundToInt()}, ${y.roundToInt()})")
    canvas.print(Vec2.screen(model.windowSize.x - 150.0, 80.0), "zoom ${model.zoom}")
}

private infix fun Canvas.drawWorld(model: Model) {
    drawQuadtree(model.world, Vec2.screen(0.0, 0.0), Vec2.screen(model.windowSize.x, model.windowSize.x))
}

private fun Canvas.drawQuadtree(qt: Quadtree, origin: Vec2<Screen>, size: Vec2<Screen>): Unit = when (qt) {
    is Leaf -> rectangle(origin, size, qt.colour)
    is Node -> {
        val smaller = 0.5 * size
        drawQuadtree(qt.children[0], origin, smaller)
        drawQuadtree(qt.children[1], origin + smaller.copy(y = 0.0), smaller)
        drawQuadtree(qt.children[2], origin + smaller.copy(x = 0.0), smaller)
        drawQuadtree(qt.children[3], origin + smaller, smaller)
        rectangleOutline(origin, size, Colour.lightBlue)
    }
}

private infix fun Canvas.drawPalette(model: Model) {
    val (ww, wh) = model.windowSize
    val (radius, _, entries) = model.palette

    entries.forEach { (_, pos, colour) ->
        // distance at which the colours disappear completely
        val falloff = min(ww, wh) / 3
        val distZeroOne = min(1.0, max(0.0, (model.cursorPos - pos).magnitude / falloff))
        val alpha = 1 - distZeroOne

        val fadingColour = colour.copy(alpha = alpha)
        circle(pos, radius, fadingColour)

        if (colour == model.currentColour)
            circleOutline(pos, radius * 1.2, 1.5, fadingColour.inverted)
    }
}