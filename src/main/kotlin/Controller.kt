import CameraModel.Companion.calculateZoom
import org.lwjgl.glfw.GLFW
import kotlin.math.sign

const val resolution = 1.0 / 128

// TODO refactor Model so that this can return sth like Pair<Camera, Quadtree>
private fun Model.drawCircle(pos: Vec2<Screen>): Model =
    paint(camera.toWorldSpace(pos), Vec2.world(resolution, resolution), Leaf(currentColour))

fun Model.update(e: Event): Model = when (e) {
    is CursorEvent -> {
        val newPos = e.p
        when (true) {
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_LEFT] -> (this + camera.copy(cursorPos = newPos)).drawCircle(newPos)
            mousePressed[GLFW.GLFW_MOUSE_BUTTON_RIGHT], mousePressed[GLFW.GLFW_MOUSE_BUTTON_MIDDLE] -> this + camera.copy(
                offset = camera.offset + newPos - camera.cursorPos,
                cursorPos = newPos
            )
            else -> this + camera.copy(cursorPos = newPos)
        }
    }
    is KeyEvent ->
        if (e.action == GLFWAction.Pressed || e.action == GLFWAction.Repeated) handleKeyPress(e)
        else this
    is MouseEvent -> handleMouseEvent(e)
    is ScrollEvent -> this + camera.handleScrollEvent(e)
    is ResizeEvent -> handleResizeEvent(e)
}

private fun CameraModel.handleScrollEvent(e: ScrollEvent): CameraModel = when {
    e.vertical != 0.0 -> updateWithZoomLevel(zoomLevel + 32 * e.vertical.sign.toInt())
    else -> this
}

private fun CameraModel.updateWithZoomLevel(zoomLevel: Int): CameraModel =
    copy(zoomLevel = zoomLevel, offset = calculateZoomOffset(zoomLevel))

fun CameraModel.calculateZoomOffset(newZoomLevel: Int, pos: Vec2<Screen> = cursorPos): Vec2<Screen> =
    offset + (calculateZoom(newZoomLevel - zoomLevel) - 1) * (offset - pos + 0.5 * squareWindowSize)

val arrowKeys: Map<Int, Pair<Int, Int>> = mapOf(
    GLFW.GLFW_KEY_UP to (0 to -1),
    GLFW.GLFW_KEY_DOWN to (0 to 1),
    GLFW.GLFW_KEY_LEFT to (-1 to 0),
    GLFW.GLFW_KEY_RIGHT to (1 to 0),
)

private fun Model.handleKeyPress(e: KeyEvent): Model = when (e.key) {
    GLFW.GLFW_KEY_Q -> copy(shouldClose = true)
    GLFW.GLFW_KEY_D -> this + flags.copy(showTreeQuadrants = !flags.showTreeQuadrants)
    in arrowKeys.keys -> this + camera.copy(offset = camera.offset + Vec2.screen(10.0 * (arrowKeys[e.key]!! map { it.toDouble() })))
    else -> this
}

private infix fun Model.handleResizeEvent(e: ResizeEvent): Model {
    val (ww, wh) = e.w to e.h
    val y = wh - palette.separation - palette.radius
    return this +
            camera.copy(windowSize = Vec2.screen(ww.toDouble(), wh.toDouble())) +
            // reposition the palette
            palette.copy(entries = palette.entries.mapIndexed { i, entry ->
                val x = ww / 2.0 + (i - palette.entries.size / 2 + 0.5) * (2 * palette.radius + palette.separation)
                entry.copy(position = Vec2.screen(x, y))
            })
}

private infix fun Model.handleMouseEvent(e: MouseEvent): Model =
    if (e.action == GLFWAction.Pressed) {
        val paletteEntry = palette.entries.firstOrNull { (r, p, _) -> (p - camera.cursorPos).magnitude <= r }
        (when {
            paletteEntry != null -> copy(currentColour = paletteEntry.colour)
            e.button == GLFW.GLFW_MOUSE_BUTTON_LEFT -> drawCircle(camera.cursorPos)
            else -> this
        }).copy(mousePressed = mousePressed + (e.button to true))
    } else copy(mousePressed = mousePressed + (e.button to false))
