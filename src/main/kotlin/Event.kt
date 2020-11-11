sealed class Event

data class CursorEvent(val p: Vec2<Screen>) : Event() {
    companion object
}

data class KeyEvent(val key: Int, val scanCode: Int, val action: GLFWAction, val mods: Int) : Event() {
    companion object
}

data class MouseEvent(val button: Int, val action: GLFWAction, val mods: Int) : Event() {
    companion object
}

data class ScrollEvent(val vertical: Double, val horizontal: Double) : Event() {
    companion object
}

data class ResizeEvent(val w: Int, val h: Int) : Event() {
    companion object
}
