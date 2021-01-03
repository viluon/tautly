data class Model(
    val world: Quadtree,
    val cursorPos: Vec2<Screen> = Vec2.zero(),
    val shouldClose: Boolean = false,
    val mousePressed: Map<Int, Boolean> = mapOf(),
    val zoom: Double = 1.0,
    val offset: Vec2<Screen> = Vec2.zero(),
    val windowSize: Vec2<Screen> = Vec2.zero(),
    val palette: PaletteModel,
    val currentColour: Colour,
    val flags: FlagModel = FlagModel(),
) {
    val squareWindowSize: Vec2<Screen> = windowSize.max.vec()
}

data class FlagModel(
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


fun Model.toWorldSpace(pos: Vec2<Screen>): Vec2<World> {
    val (x, y) = 1 / zoom * (2.0 * (pos - offset) / squareWindowSize - Vec2.screen(1.0, 1.0))
    return Vec2.world(x, y)
}

fun Model.toScreenSpace(pos: Vec2<World>, zoom: Double = this.zoom): Vec2<Screen> {
    val (x, y) = pos
    return 0.5 * (zoom * Vec2.screen(x, y) + Vec2.screen(1.0, 1.0)) * squareWindowSize + offset
}
