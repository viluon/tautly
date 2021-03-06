import kotlin.math.max
import kotlin.math.min

fun view(canvas: Canvas, model: Model) {
    canvas drawWorld model
    canvas drawPalette model

    val camera = model.camera
    val (cx, cy) = camera.toWorldSpace(camera.cursorPos)
    val cursor = "(${cx round 5}, ${cy round 5})"
    canvas.print(Vec2.screen(camera.windowSize.x - 150.0 - 5 * cursor.length, 30.0), cursor)

    val (x, y) = camera.offset
    val offset = "($x, $y)"
    canvas.print(Vec2.screen(camera.windowSize.x - 150.0 - 5 * offset.length, 50.0), offset)
    canvas.print(Vec2.screen(camera.windowSize.x - 150.0, 80.0), "zoom ${camera.zoom}")
}

private infix fun Canvas.drawWorld(model: Model) = let { canvas ->
    model.camera.run {
        canvas.drawQuadtree(model.flags,
            model.world,
            -0.5 * zoom * squareWindowSize + toScreenSpace(Vec2.zero()),
            zoom * squareWindowSize)
    }
}

private fun Canvas.drawQuadtree(flags: FlagsModel, qt: Quadtree, origin: Vec2<Screen>, size: Vec2<Screen>): Unit = when {
    qt is Leaf || size.max < 20.0 -> {
        // we apply a subpixel overlap to make sure there are no seams in the rendering
        val overlap = 1.25 * Vec2.screen(.5, .5)
        rectangle(origin - overlap, size + overlap, qt.colour)
    }
    qt is Node -> {
        val smaller = 0.5 * size
        drawQuadtree(flags, qt.children[0], origin, smaller)
        drawQuadtree(flags, qt.children[1], origin + smaller.copy(y = 0.0), smaller)
        drawQuadtree(flags, qt.children[2], origin + smaller.copy(x = 0.0), smaller)
        drawQuadtree(flags, qt.children[3], origin + smaller, smaller)
        if (flags.showTreeQuadrants) rectangleOutline(origin, size, Colour.lightBlue) else Unit
    }
    else -> throw IllegalStateException()
}

private infix fun Canvas.drawPalette(model: Model) {
    val (ww, wh) = model.camera.windowSize
    val (radius, _, entries) = model.palette

    entries.forEach { (_, pos, colour) ->
        // distance at which the colours disappear completely
        val falloff = min(ww, wh) / 3
        val distZeroOne = min(1.0, max(0.0, (model.camera.cursorPos - pos).magnitude / falloff))
        val alpha = 1 - distZeroOne

        val fadingColour = colour.copy(alpha = alpha)
        circle(pos, radius, fadingColour)

        if (colour == model.currentColour)
            circleOutline(pos, radius * 1.2, 1.5, fadingColour.inverted)
    }
}
