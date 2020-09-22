sealed class Event

data class CursorEvent(val x: Double, val y: Double) : Event()
data class KeyEvent(val key: Int, val scanCode: Int, val action: GLFWAction, val mods: Int) : Event()
data class MouseEvent(val button: Int, val action: GLFWAction, val mods: Int) : Event()
