import Colour.Companion.black
import Colour.Companion.darkGrayishBlue
import Colour.Companion.lightBlue
import Colour.Companion.lightGreen
import Colour.Companion.magenta
import Colour.Companion.orange
import Colour.Companion.white
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

// TODO add an animated Tautly logo when opening the app and clear it with an invisible sponge
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
        world = Leaf(darkGrayishBlue),
        currentColour = white,
        palette = PaletteModel(radius, 40.0,
            listOf(white, lightGreen, lightBlue, magenta, orange, black).map { colour ->
                Circle(radius, Vec2.screen(0.0, 0.0), colour)
            }
        )
    )

    setUpWindowHints()
    val window = setUpWindow()
    setUpCallbacks(window) { 
        model = model.update(it)
    }

    val nvgContext = createNvgContext()
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
        glfwWaitEventsTimeout(1.0)

        buffers.forEach { it.flip() }
    }

    canvas.free()
    nvgDelete(nvgContext)
    glfwDestroyWindow(window)
}

private fun createNvgContext(): Long {
    val nvgContext = nvgCreate(NVG_ANTIALIAS or NVG_STENCIL_STROKES or NVG_DEBUG)
    println("loading fonts")
    NanoVG.nvgCreateFont(
        nvgContext,
        "iosevka",
        "/usr/share/fonts/TTF/iosevka-sparkle-regular.ttf"
    ).takeIf { it >= 0 } ?: throw RuntimeException("could not load font")

    NanoVG.nvgFontSize(nvgContext, 22f)
    NanoVG.nvgFontFace(nvgContext, "iosevka")
    NanoVG.nvgFontBlur(nvgContext, 0f)
    println("fonts loaded")
    return nvgContext
}

private inline fun setUpCallbacks(window: Long, crossinline mutate: (Event) -> Unit) {
    glfwSetKeyCallback(window) { _, key, scanCode, action, mods ->
        mutate(KeyEvent(key, scanCode, parseAction(action), mods))
    }
    glfwSetCursorPosCallback(window) { _, x, y ->
        mutate(CursorEvent(Vec2.screen(x, y)))
    }
    glfwSetMouseButtonCallback(window) { _, button, action, mods ->
        mutate(MouseEvent(button, parseAction(action), mods))
    }
    glfwSetScrollCallback(window) { _, horizontal, vertical ->
        mutate(ScrollEvent(vertical, horizontal))
    }
    glfwSetWindowSizeCallback(window) { _, w, h ->
        mutate(ResizeEvent(w, h))
    }
}

private fun allocateBuffers(): List<IntBuffer> = generateSequence { MemoryUtil.memAllocInt(1) }.take(4).toList()

private fun setUpWindow(): Long {
    val (w, h) = 640 to 480
    val window = glfwCreateWindow(w, h, "Tautly", NULL, NULL)
    if (window == NULL) throw RuntimeException("could not open a window")

    val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        ?: throw RuntimeException("could not read the video mode of the primary monitor")

    glfwSetWindowPos(window, (videoMode.width() - w) / 2, (videoMode.height() - h) / 2)
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
