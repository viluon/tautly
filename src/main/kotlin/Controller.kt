import org.lwjgl.glfw.GLFW
import kotlin.math.pow
import kotlin.math.sqrt

private infix fun Pair<Float, Float>.distance(other: Pair<Double, Double>): Float = sqrt(
    (first - other.first.toFloat()).pow(2) + (second - other.second.toFloat()).pow(2)
)

private fun Model.addCircle(pos: Pair<Double, Double>): Model = copy(
    circles = circles + Circle(3f, pos map { it.toFloat() }, currentColour)
)

fun Model.update(e: Event): Model = when (e) {
    is CursorEvent -> {
        val lastPos = e.x to e.y
        if (mousePressed[GLFW.GLFW_MOUSE_BUTTON_LEFT] == true) addCircle(lastPos).copy(cursorPos = lastPos)
        else copy(cursorPos = lastPos)
    }
    is KeyEvent ->
        if (e.action == GLFWAction.Pressed && e.key == GLFW.GLFW_KEY_Q) copy(shouldClose = true)
        else this
    is MouseEvent -> this handleMouseEvent e
    is ScrollEvent -> this
    is ResizeEvent -> this handleResizeEvent e
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
            e.button == GLFW.GLFW_MOUSE_BUTTON_LEFT -> addCircle(cursorPos)
            else -> this
        }
    }()).copy(mousePressed = mousePressed + (e.button to true))
    else copy(mousePressed = mousePressed + (e.button to false))