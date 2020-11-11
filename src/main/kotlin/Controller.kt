import org.lwjgl.glfw.GLFW

private fun Model.drawCircle(pos: Vec2<Screen>): Model = copy(
    world = world.paint(toWorldSpace(pos), Vec2.world(0.25, 0.25), Leaf(currentColour))
)

fun Model.update(e: Event): Model = when (e) {
    is CursorEvent -> {
        val newPos = e.p
        when (true) {
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_LEFT] -> drawCircle(newPos).copy(cursorPos = newPos)
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_RIGHT] -> copy(
                offset = toWorldSpace(toScreenSpace(offset) + newPos - cursorPos),
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
    offset = toWorldSpace(toScreenSpace(offset) + calculateZoomOffset(zoom))
)

private fun Model.calculateZoomOffset(newZoom: Double): Vec2<Screen> = (1 - newZoom / zoom) * cursorPos

val arrowKeys: Map<Int, Pair<Int, Int>> = mapOf(
    GLFW.GLFW_KEY_UP to (0 to -1),
    GLFW.GLFW_KEY_DOWN to (0 to 1),
    GLFW.GLFW_KEY_LEFT to (-1 to 0),
    GLFW.GLFW_KEY_RIGHT to (1 to 0),
)

@Suppress("MapGetWithNotNullAssertionOperator")
private fun Model.handleKeyPress(e: KeyEvent): Model = when (e.key) {
    GLFW.GLFW_KEY_Q -> copy(shouldClose = true)
    in arrowKeys.keys -> copy(offset = offset + Vec2.world(10.0 * (arrowKeys[e.key]!! map { it.toDouble() })))
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
    if (e.action == GLFWAction.Pressed) ({
        val paletteEntry = palette.entries.firstOrNull { (r, p, _) -> (p - cursorPos).magnitude <= r }
        when {
            paletteEntry != null -> copy(currentColour = paletteEntry.colour)
            e.button == GLFW.GLFW_MOUSE_BUTTON_LEFT -> drawCircle(cursorPos)
            else -> this
        }
    }()).copy(mousePressed = mousePressed + (e.button to true))
    else copy(mousePressed = mousePressed + (e.button to false))
