data class Model(
    val world: Quadtree,
    val cursorPos: Vec2<Screen> = Vec2.screen(0.0, 0.0),
    val shouldClose: Boolean = false,
    val mousePressed: Map<Int, Boolean> = mapOf(),
    val zoom: Double = 1.0,
    val offset: Vec2<World> = Vec2.world(0.0, 0.0),
    val windowSize: Vec2<Screen> = Vec2.screen(0.0, 0.0),
    val palette: PaletteModel,
    val currentColour: Colour,
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
    val (x, y) = 2.0 * pos / windowSize - Vec2.screen(1.0, 1.0)
    return 1 / zoom * Vec2.world(x, y) - offset
}

fun Model.toScreenSpace(pos: Vec2<World>): Vec2<Screen> {
    val (x, y) = pos + offset
    return 0.5 * (zoom * Vec2.screen(x, y) + Vec2.screen(1.0, 1.0)) * windowSize
}
