sealed class Event

data class CursorEvent(val p: ScreenSpace) : Event()
data class KeyEvent(val key: Int, val scanCode: Int, val action: GLFWAction, val mods: Int) : Event()
data class MouseEvent(val button: Int, val action: GLFWAction, val mods: Int) : Event()
data class ScrollEvent(val vertical: Double, val horizontal: Double) : Event()
data class ResizeEvent(val w: Int, val h: Int) : Event()
