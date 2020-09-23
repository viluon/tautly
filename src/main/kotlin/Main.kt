import GLFWAction.*
import org.lwjgl.Version
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVG.nvgBeginFrame
import org.lwjgl.nanovg.NanoVG.nvgEndFrame
import org.lwjgl.nanovg.NanoVGGL3.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.IntBuffer

fun main() {
    println("launching Tautly with LWJGL ${Version.getVersion()}")
    glfw()
}

private fun glfw() {
    val errorCallback = GLFWErrorCallback.createPrint(System.err)
    glfwSetErrorCallback(errorCallback)
    if (!glfwInit()) throw IllegalStateException("could not init GLFW")

    try {
        loop()
    } finally {
        glfwTerminate()
        errorCallback.free()
    }
}

private fun loop() {
    val radius = 20.0

    var model = Model(
        currentColour = Triple(0.0, 0.0, 1.0),
        palette = PaletteModel(radius, 40.0,
            listOf(
                Triple(0.0, 0.0, 1.0),
                Triple(68 / 360.0, .74, .54),
                Triple(191 / 360.0, .74, .54),
                Triple(323 / 360.0, .74, .54),
                Triple(12 / 360.0, .74, .54),
                Triple(0.0, 0.0, 0.0),
            ).map { colour ->
                Circle(radius, ScreenSpace(0.0 to 0.0), colour)
            }
        )
    )

    infix fun Model.mutate(ev: Event): Unit = {
        model = update(ev)
    }()

    setUpWindowHints()
    val window = setUpWindow()

    glfwSetKeyCallback(window) { _, key, scanCode, action, mods ->
        model mutate KeyEvent(key, scanCode, parseAction(action), mods)
    }
    glfwSetCursorPosCallback(window) { _, x, y ->
        model mutate CursorEvent(ScreenSpace(x to y))
    }
    glfwSetMouseButtonCallback(window) { _, button, action, mods ->
        model mutate MouseEvent(button, parseAction(action), mods)
    }
    glfwSetScrollCallback(window) { _, horizontal, vertical ->
        model mutate ScrollEvent(vertical, horizontal)
    }
    glfwSetWindowSizeCallback(window) { _, w, h ->
        model mutate ResizeEvent(w, h)
    }

    val nvgContext = nvgCreate(NVG_ANTIALIAS or NVG_STENCIL_STROKES or NVG_DEBUG)
    println("loading fonts")
    val font = NanoVG.nvgCreateFont(nvgContext, "iosevka", "/usr/share/fonts/TTF/iosevka-sparkle-regular.ttf")
    if (font < 0) throw RuntimeException("could not load font")
    NanoVG.nvgFontSize(nvgContext, 22f)
    NanoVG.nvgFontFace(nvgContext, "iosevka")
    NanoVG.nvgFontBlur(nvgContext, 0f)
    println("fonts loaded")

    val canvas = Canvas `in` nvgContext

    val buffers = allocateBuffers()
    val (w, h, windowW, windowH) = buffers

    while (!model.shouldClose && !glfwWindowShouldClose(window)) {
        glfwGetFramebufferSize(window, w, h)
        glfwGetWindowSize(window, windowW, windowH)

        // Nano VG example says this is for hi-dpi devices
        val pixelRatio = w.get().toFloat() / h.get()
        buffers.forEach { it.rewind() }

        glViewport(0, 0, w.get(), h.get())
        glClearColor(0x2e / 256f, 0x34 / 256f, 0x40 / 256f, 1f)
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

private fun allocateBuffers(): List<IntBuffer> = generateSequence { MemoryUtil.memAllocInt(1) }.take(4).toList()

private fun setUpWindow(): Long {
    val window = glfwCreateWindow(640, 480, "Tautly", NULL, NULL)
    if (window == NULL) throw RuntimeException("could not open a window")

    val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        ?: throw RuntimeException("could not read the video mode of the primary monitor")

    glfwSetWindowPos(window, (videoMode.width() - 640) / 2, (videoMode.height() - 480) / 2)
    glfwMakeContextCurrent(window)
    GL.createCapabilities()
    glfwSwapInterval(1)
    return window
}

private fun setUpWindowHints() {
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
    glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, 1)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_SAMPLES, 4)
}

private fun parseAction(rawAction: Int): GLFWAction = when (rawAction) {
    GLFW_RELEASE -> Released
    GLFW_PRESS -> Pressed
    else -> Repeated
}
