import org.lwjgl.glfw.GLFW.*

enum class GLFWAction(val code: Int) {
    Pressed(GLFW_PRESS), Repeated(GLFW_REPEAT), Released(GLFW_RELEASE)
}
