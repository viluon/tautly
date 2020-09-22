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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
    println("launching Tautly with LWJGL ${Version.getVersion()}")

    glfw()
}

private fun Model.addCircle(x: Double, y: Double): Model = copy(
    circles = circles + Circle(3f, x.toFloat() to y.toFloat(), currentColour)
)

private fun controller(e: Event, model: Model): Model = when (e) {
    is CursorEvent -> {
        val lastPos = e.x to e.y
        if (model.mousePressed[GLFW_MOUSE_BUTTON_LEFT] == true) model.addCircle(e.x, e.y).copy(cursorPos = lastPos)
        else model.copy(cursorPos = lastPos)
    }
    is KeyEvent ->
        if (e.action == Pressed && e.key == GLFW_KEY_Q) model.copy(shouldClose = true)
        else model
    is MouseEvent -> model handleMouseEvent e
    is ScrollEvent -> model
    is ResizeEvent -> model handleResizeEvent e
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

private infix fun Pair<Float, Float>.distance(other: Pair<Double, Double>): Float = sqrt(
    (first - other.first.toFloat()).pow(2) + (second - other.second.toFloat()).pow(2)
)

private infix fun Model.handleMouseEvent(e: MouseEvent): Model {
    val (x, y) = cursorPos
    return if (e.action == Pressed) ({
        val paletteEntry = palette.entries.firstOrNull { (r, p, _) -> p distance cursorPos <= r }
        when {
            paletteEntry != null -> copy(currentColour = paletteEntry.colour)
            e.button == GLFW_MOUSE_BUTTON_LEFT -> addCircle(x, y)
            else -> this
        }
    }()).copy(mousePressed = mousePressed + (e.button to true))
    else copy(mousePressed = mousePressed + (e.button to false))
}

private fun view(canvas: Canvas, model: Model) {
    model.circles.forEach { (r, pos, colour) ->
        val (x, y) = pos
        val (h, s, l) = colour
        canvas.circle(x, y, r, h, s, l, 1f)
    }

    val (ww, wh) = model.windowSize
    val (radius, _, entries) = model.palette

    entries.forEachIndexed { i, (_, pos, colour) ->
        val (h, s, l) = colour
        val (curX, curY) = model.cursorPos
        val (x, y) = pos

        val dist = sqrt((curX - x).pow(2) + (curY - y).pow(2))

        // distance at which the colours disappear completely
        val falloff = min(ww, wh) / 3
        val distZeroOne = min(1.0, max(0.0, dist / falloff))
        val alpha = 1 - distZeroOne

        canvas.circle(x, y, radius, h, s, l, alpha.toFloat())
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
    val radius = 20f

    var model = Model(
        currentColour = Triple(0f, 0f, 1f),
        palette = PaletteModel(20f, 40f,
            listOf(
                Triple(0f, 0f, 1f),
                Triple(68 / 360f, .74f, .54f),
                Triple(191 / 360f, .74f, .54f),
                Triple(323 / 360f, .74f, .54f),
                Triple(12 / 360f, .74f, .54f),
                Triple(0f, 0f, 0f),
            ).map { colour ->
                Circle(radius, 0f to 0f, colour)
            }
        )
    )

    infix fun Model.update(ev: Event): Unit = {
        model = controller(ev, this)
    }()

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
        model update KeyEvent(key, scanCode, parseAction(action), mods)
    }
    glfwSetCursorPosCallback(window) { _, x, y ->
        model update CursorEvent(x, y)
    }
    glfwSetMouseButtonCallback(window) { _, button, action, mods ->
        model update MouseEvent(button, parseAction(action), mods)
    }
    glfwSetScrollCallback(window) { _, vertical, horizontal ->
        model update ScrollEvent(vertical, horizontal)
    }
    glfwSetWindowSizeCallback(window) { _, w, h ->
        model update ResizeEvent(w, h)
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

private fun parseAction(rawAction: Int): GLFWAction = when (rawAction) {
    GLFW_RELEASE -> Released
    GLFW_PRESS -> Pressed
    else -> Repeated
}
