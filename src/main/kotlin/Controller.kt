import org.lwjgl.glfw.GLFW
import kotlin.math.pow
import kotlin.math.sqrt

private fun Model.drawCircle(pos: ScreenSpace): Model = copy(
    circles = circles + Circle(3.0, toWorldSpace(pos), currentColour)
)

fun Model.update(e: Event): Model = when (e) {
    is CursorEvent -> {
        val newPos = e.p
        when (true) {
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_LEFT] -> drawCircle(newPos).copy(cursorPos = newPos)
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_RIGHT] -> copy(
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
    offset = offset + calculateZoomOffset(zoom)
)

private fun Model.calculateZoomOffset(newZoom: Double): ScreenSpace {
    val normalisedCursorPos = cursorPos / ScreenSpace(windowSize.map { it.toDouble() })
    val diagonal = sqrt(windowSize.first.toDouble().pow(2) + windowSize.second.toDouble().pow(2))
    return (diagonal * zoom - diagonal * newZoom) * normalisedCursorPos
}

val arrowKeys: Map<Int, Pair<Int, Int>> = mapOf(
    GLFW.GLFW_KEY_UP to (0 to -1),
    GLFW.GLFW_KEY_DOWN to (0 to 1),
    GLFW.GLFW_KEY_LEFT to (-1 to 0),
    GLFW.GLFW_KEY_RIGHT to (1 to 0),
)

@Suppress("MapGetWithNotNullAssertionOperator")
private fun Model.handleKeyPress(e: KeyEvent): Model = when (e.key) {
    GLFW.GLFW_KEY_Q -> copy(shouldClose = true)
    in arrowKeys.keys -> copy(offset = offset + ScreenSpace(10.0 * (arrowKeys[e.key]!! map { it.toDouble() })))
    else -> this
}

private infix fun Model.handleResizeEvent(e: ResizeEvent): Model {
    val (ww, wh) = e.w to e.h
    val y = wh - palette.separation - palette.radius
    // reposition the palette
    return copy(windowSize = e.w to e.h, palette = palette.copy(entries = palette.entries.mapIndexed { i, entry ->
        val x = ww / 2.0 + (i - palette.entries.size / 2 + 0.5) * (2 * palette.radius + palette.separation)
        entry.copy(position = ScreenSpace(x to y))
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
