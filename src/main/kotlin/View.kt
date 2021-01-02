import kotlin.math.max
import kotlin.math.min

fun view(canvas: Canvas, model: Model) {
    canvas drawWorld model
    canvas drawPalette model

    val cursor = "(${model.cursorPos.x}, ${model.cursorPos.y})"
    canvas.print(Vec2.screen(model.windowSize.x - 150.0 - 5 * cursor.length, 30.0), cursor)

    val (x, y) = model.offset
    val offset = "($x, $y)"
    canvas.print(Vec2.screen(model.windowSize.x - 150.0 - 5 * offset.length, 50.0), offset)
    canvas.print(Vec2.screen(model.windowSize.x - 150.0, 80.0), "zoom ${model.zoom}")
}

private infix fun Canvas.drawWorld(model: Model) = let { canvas ->
    model.run {
        canvas.drawQuadtree(world, -0.5 * zoom * windowSize + toScreenSpace(Vec2.world(0.0, 0.0)), zoom * windowSize)
    }
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