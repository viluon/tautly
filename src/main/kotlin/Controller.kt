import org.lwjgl.glfw.GLFW
import kotlin.math.pow
import kotlin.math.sqrt

private infix fun Pair<Float, Float>.distance(other: Pair<Double, Double>): Float = sqrt(
    (first - other.first.toFloat()).pow(2) + (second - other.second.toFloat()).pow(2)
)

private fun Model.drawCircle(pos: Pair<Double, Double>): Model = copy(
    circles = circles + Circle(3f, (1 / zoom) * pos.map { it.toFloat() } - offset, currentColour)
)

fun Model.update(e: Event): Model = when (e) {
    is CursorEvent -> {
        val newPos = e.x to e.y
        when (true) {
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_LEFT] -> drawCircle(newPos).copy(cursorPos = newPos)
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_RIGHT] -> copy(
                offset = offset + newPos.map { it.toFloat() } - cursorPos.map { it.toFloat() },
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
    e.vertical < 0 -> updateWithZoom(zoom * 0.9f)
    e.vertical > 0 -> updateWithZoom(zoom * 1.1f)
    else -> this
}

private fun Model.updateWithZoom(zoom: Float): Model = copy(
    zoom = zoom,
    offset = offset + calculateZoomOffset(zoom)
)

private fun Model.calculateZoomOffset(newZoom: Float): Pair<Float, Float> {
    val normalisedCursorPos = cursorPos.map { it.toFloat() } / windowSize.map { it.toFloat() }
    val diagonal = sqrt(windowSize.first.toDouble().pow(2) + windowSize.second.toDouble().pow(2)).toFloat()
    return (diagonal * zoom - diagonal * newZoom) * normalisedCursorPos
}

val arrowKeys: Map<Int, Pair<Int, Int>> = mapOf(
    GLFW.GLFW_KEY_UP to (0 to -1),
    GLFW.GLFW_KEY_DOWN to (0 to 1),
    GLFW.GLFW_KEY_LEFT to (-1 to 0),
    GLFW.GLFW_KEY_RIGHT to (1 to 0),
)

private fun Model.handleKeyPress(e: KeyEvent): Model = when (e.key) {
    GLFW.GLFW_KEY_Q -> copy(shouldClose = true)
    in arrowKeys.keys -> copy(offset = offset + 10f * (arrowKeys[e.key]!! map { it.toFloat() }))
    else -> this
}

private infix fun Model.handleResizeEvent(e: ResizeEvent): Model {
    val (ww, wh) = e.w to e.h
    val y = wh - palette.separation - palette.radius
    // reposition the palette
    return copy(windowSize = e.w to e.h, palette = palette.copy(entries = palette.entries.mapIndexed { i, entry ->
        val x = ww / 2f + (i - palette.entries.size / 2 + 0.5f) * (2 * palette.radius + palette.separation)
        entry.copy(position = x to y)
    }))
}

private infix fun Model.handleMouseEvent(e: MouseEvent): Model =
    if (e.action == GLFWAction.Pressed) ({
        val paletteEntry = palette.entries.firstOrNull { (r, p, _) -> p distance cursorPos <= r }
        when {
            paletteEntry != null -> copy(currentColour = paletteEntry.colour)
            e.button == GLFW.GLFW_MOUSE_BUTTON_LEFT -> drawCircle(cursorPos)
            else -> this
        }
    }()).copy(mousePressed = mousePressed + (e.button to true))
    else copy(mousePressed = mousePressed + (e.button to false))
