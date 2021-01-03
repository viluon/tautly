import org.lwjgl.glfw.GLFW

const val resolution = 1.0 / 128

// TODO refactor Model so that this can return sth like Pair<Camera, Quadtree>
private fun Model.drawCircle(pos: Vec2<Screen>): Model =
    paint(toWorldSpace(pos), Vec2.world(resolution, resolution), Leaf(currentColour))

fun Model.update(e: Event): Model = when (e) {
    is CursorEvent -> {
        val newPos = e.p
        when (true) {
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_LEFT] -> drawCircle(newPos).copy(cursorPos = newPos)
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_RIGHT], mousePressed[GLFW.GLFW_MOUSE_BUTTON_MIDDLE] -> copy(
                offset = offset + newPos - cursorPos,
                cursorPos = newPos
            )
            else -> copy(cursorPos = newPos)
        }
    }
    is KeyEvent ->
        if (e.action == GLFWAction.Pressed || e.action == GLFWAction.Repeated) handleKeyPress(e)
        else this
    is MouseEvent -> handleMouseEvent(e)
    is ScrollEvent -> handleScrollEvent(e)
    is ResizeEvent -> handleResizeEvent(e)
}

private fun Model.handleScrollEvent(e: ScrollEvent): Model = when {
    e.vertical < 0 -> updateWithZoom(zoom * 0.9)
    e.vertical > 0 -> updateWithZoom(zoom * 1.1)
    else -> this
}

private fun Model.updateWithZoom(zoom: Double): Model = copy(
    zoom = zoom,
    offset = calculateZoomOffset(zoom)
)

fun Model.calculateZoomOffset(newZoom: Double, pos: Vec2<Screen> = cursorPos): Vec2<Screen> =
    calculateZoomOffset(zoom, newZoom, offset, pos, squareWindowSize)

fun calculateZoomOffset(
    zoom: Double,
    newZoom: Double,
    offset: Vec2<Screen>,
    pos: Vec2<Screen>,
    squareWindowSize: Vec2<Screen>,
): Vec2<Screen> =
    offset + (newZoom / zoom - 1) * (offset - pos + 0.5 * squareWindowSize)

val arrowKeys: Map<Int, Pair<Int, Int>> = mapOf(
    GLFW.GLFW_KEY_UP to (0 to -1),
    GLFW.GLFW_KEY_DOWN to (0 to 1),
    GLFW.GLFW_KEY_LEFT to (-1 to 0),
    GLFW.GLFW_KEY_RIGHT to (1 to 0),
)

@Suppress("MapGetWithNotNullAssertionOperator")
private fun Model.handleKeyPress(e: KeyEvent): Model = when (e.key) {
    GLFW.GLFW_KEY_Q -> copy(shouldClose = true)
    GLFW.GLFW_KEY_D -> copy(flags = flags.copy(showTreeQuadrants = !flags.showTreeQuadrants))
    in arrowKeys.keys -> copy(offset = offset + Vec2.screen(10.0 * (arrowKeys[e.key]!! map { it.toDouble() })))
    else -> this
}

private infix fun Model.handleResizeEvent(e: ResizeEvent): Model {
    val (ww, wh) = e.w to e.h
    val y = wh - palette.separation - palette.radius
    return copy(
        windowSize = Vec2.screen(ww.toDouble(), wh.toDouble()),
        // reposition the palette
        palette = palette.copy(entries = palette.entries.mapIndexed { i, entry ->
            val x = ww / 2.0 + (i - palette.entries.size / 2 + 0.5) * (2 * palette.radius + palette.separation)
            entry.copy(position = Vec2.screen(x, y))
        }))
}

private infix fun Model.handleMouseEvent(e: MouseEvent): Model =
    if (e.action == GLFWAction.Pressed) {
        val paletteEntry = palette.entries.firstOrNull { (r, p, _) -> (p - cursorPos).magnitude <= r }
        (when {
            paletteEntry != null -> copy(currentColour = paletteEntry.colour)
            e.button == GLFW.GLFW_MOUSE_BUTTON_LEFT -> drawCircle(cursorPos)
            else -> this
        }).copy(mousePressed = mousePressed + (e.button to true))
    } else copy(mousePressed = mousePressed + (e.button to false))
