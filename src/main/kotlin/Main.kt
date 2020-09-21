import org.lwjgl.Version
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWKeyCallbackI
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL


fun main() {
    println("launching Tautly with LWJGL ${Version.getVersion()}")

    val keyCallback = GLFWKeyCallbackI { window, key, _, action, _ ->
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true)
        }
    }

    val errorCallback = GLFWErrorCallback.createPrint(System.err)
    glfwSetErrorCallback(errorCallback)

    if (!glfwInit()) {
        throw IllegalStateException("could not init GLFW")
    }

    val window = glfwCreateWindow(640, 480, "Tautly", NULL, NULL)
    if (window == NULL) {
        glfwTerminate()
        throw RuntimeException("could not open a window")
    }

    val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        ?: throw RuntimeException("could not read the video mode of the primary monitor")

    glfwSetWindowPos(window, (videoMode.width() - 640) / 2, (videoMode.height() - 480) / 2)
    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(1)
    glfwSetKeyCallback(window, keyCallback)

    val w = MemoryUtil.memAllocInt(1)
    val h = MemoryUtil.memAllocInt(1)

    while (!glfwWindowShouldClose(window)) {
        glfwGetFramebufferSize(window, w, h)
        val ratio = w.get().toDouble() / h.get()
        w.rewind()
        h.rewind()

        glViewport(0, 0, w.get(), h.get())
        glClear(GL_COLOR_BUFFER_BIT)

        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(-ratio, ratio, -1.0, 1.0, 1.0, -1.0)
        glMatrixMode(GL_MODELVIEW)

        glLoadIdentity()
        glRotatef(glfwGetTime().toFloat() * 50f, 0f, 0f, 1f)

        glBegin(GL_TRIANGLES)
        glColor3f(1f, 0f, 0f)
        glVertex3f(-0.6f, -0.4f, 0f)
        glColor3f(0f, 1f, 0f)
        glVertex3f(0.6f, -0.4f, 0f)
        glColor3f(0f, 0f, 1f)
        glVertex3f(0f, 0.6f, 0f)
        glEnd()

        glfwSwapBuffers(window)
        glfwPollEvents()

        w.flip()
        h.flip()
    }

    glfwDestroyWindow(window)
    glfwTerminate()
    errorCallback.free()
}
