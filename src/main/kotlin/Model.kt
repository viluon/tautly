import kotlin.math.log
import kotlin.math.pow
import kotlin.math.roundToInt

data class Model(
    val world: Quadtree,
    val shouldClose: Boolean = false,
    val mousePressed: Map<Int, Boolean> = mapOf(),
    val currentColour: Colour,
    val palette: PaletteModel,
    val camera: CameraModel = CameraModel(),
    val flags: FlagsModel = FlagsModel(),
) {
    operator fun plus(cam: CameraModel) = copy(camera = cam)
    operator fun plus(flags: FlagsModel) = copy(flags = flags)
    operator fun plus(palette: PaletteModel) = copy(palette = palette)
}

// holds information specific to the way the canvas (Quadtree) is rendered to the screen,
// independently of its contents
data class CameraModel(
    val zoomLevel: Int = 0,
    val offset: Vec2<Screen> = Vec2.zero(),
    val cursorPos: Vec2<Screen> = Vec2.zero(),
    val windowSize: Vec2<Screen> = Vec2.zero(),
) {
    companion object {
        const val zoomBase = 1 + 1 / 256.0

        fun calculateZoom(level: Int): Double = zoomBase.pow(level)
        fun calculateZoomLevel(zoom: Double): Int = log(zoom, zoomBase).roundToInt()
    }

    val squareWindowSize: Vec2<Screen> = windowSize.max.vec()
    val zoom: Double = calculateZoom(zoomLevel)
}

data class FlagsModel(
    val showTreeQuadrants: Boolean = false,
)

data class PaletteModel(
    val radius: Double,
    val separation: Double,
    val entries: List<Circle<Screen>> = listOf(),
)

data class Circle<S : Space>(
    val radius: Double,
    val position: Vec2<S>,
    val colour: Colour,
)


fun CameraModel.toWorldSpace(pos: Vec2<Screen>): Vec2<World> {
    val (x, y) = 1 / zoom * (2.0 * (pos - offset) / squareWindowSize - Vec2.screen(1.0, 1.0))
    return Vec2.world(x, y)
}

fun CameraModel.toScreenSpace(pos: Vec2<World>, zoom: Double = this.zoom): Vec2<Screen> {
    val (x, y) = pos
    return 0.5 * (zoom * Vec2.screen(x, y) + Vec2.screen(1.0, 1.0)) * squareWindowSize + offset
}
