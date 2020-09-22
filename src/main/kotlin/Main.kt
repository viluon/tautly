import GLFWAction.*
import org.lwjgl.Version
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.nanovg.NanoVG.nvgBeginFrame
import org.lwjgl.nanovg.NanoVG.nvgEndFrame
import org.lwjgl.nanovg.NanoVGGL3.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.random.Random
import kotlin.random.nextInt

fun main() {
    println("launching Tautly with LWJGL ${Version.getVersion()}")

    glfw()
}

private fun controller(e: Event, model: Model): Model = when (e) {
    is CursorEvent ->
        if (model.mousePressed) model.copy(circles = model.circles + Triple(e.x.toFloat(), e.y.toFloat(), 10f))
        else model
    is KeyEvent ->
        if (e.action == Pressed && e.key == GLFW_KEY_Q) model.copy(shouldClose = true)
        else model
    is MouseEvent -> model.copy(mousePressed = e.action == Pressed)
}

private fun view(canvas: Canvas, model: Model) {
    val rng = Random(42)
    val byte = { rng.nextInt(-128..127).toByte() }
    model.circles.forEach { (cx, cy, rad) ->
        canvas.circle(cx, cy, rad, byte(), byte(), byte(), 110)
    }
}

private fun glfw() {
    val errorCallback = GLFWErrorCallback.createPrint(System.err)
    glfwSetErrorCallback(errorCallback)

    if (!glfwInit()) {
        throw IllegalStateException("could not init GLFW")
    }

    try {
        runGlfw()
    } finally {
        glfwTerminate()
        errorCallback.free()
    }
}

private fun runGlfw() {
    var model = Model()

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
    glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, 1)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_SAMPLES, 4)

    val window = glfwCreateWindow(640, 480, "Tautly", NULL, NULL)
    if (window == NULL) throw RuntimeException("could not open a window")

    val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        ?: throw RuntimeException("could not read the video mode of the primary monitor")

    glfwSetWindowPos(window, (videoMode.width() - 640) / 2, (videoMode.height() - 480) / 2)
    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(1)
    glfwSetKeyCallback(window) { _, key, scanCode, action, mods ->
        model = controller(KeyEvent(key, scanCode, parseAction(action), mods), model)
    }
    glfwSetCursorPosCallback(window) { _, x, y ->
        model = controller(CursorEvent(x, y), model)
    }
    glfwSetMouseButtonCallback(window) { _, button, action, mods ->
        model = controller(MouseEvent(button, parseAction(action), mods), model)
    }

    val nvgContext = nvgCreate(NVG_ANTIALIAS or NVG_STENCIL_STROKES or NVG_DEBUG)

    val w = MemoryUtil.memAllocInt(1)
    val h = MemoryUtil.memAllocInt(1)
    val windowW = MemoryUtil.memAllocInt(1)
    val windowH = MemoryUtil.memAllocInt(1)

    val cursorX = MemoryUtil.memAllocDouble(1)
    val cursorY = MemoryUtil.memAllocDouble(1)

    val buffers = listOf(w, h, windowW, windowH, cursorX, cursorY)

    val canvas = Canvas `in` nvgContext

    while (!model.shouldClose && !glfwWindowShouldClose(window)) {
        glfwGetFramebufferSize(window, w, h)
        glfwGetWindowSize(window, windowW, windowH)

        // Nano VG example says this is for hi-dpi devices
        val pixelRatio = w.get().toFloat() / h.get()
        buffers.forEach { it.rewind() }

        glViewport(0, 0, w.get(), h.get())
        glClearColor(0f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

        nvgBeginFrame(nvgContext, windowW.get().toFloat(), windowH.get().toFloat(), pixelRatio)
        view(canvas, model)
        nvgEndFrame(nvgContext)

        glfwSwapBuffers(window)
        glfwPollEvents()

        buffers.forEach { it.flip() }
    }

    canvas.free()
    nvgDelete(nvgContext)
    glfwDestroyWindow(window)
}

private fun parseAction(rawAction: Int): GLFWAction = when (rawAction) {
    GLFW_RELEASE -> Released
    GLFW_PRESS -> Pressed
    else -> Repeated
}
