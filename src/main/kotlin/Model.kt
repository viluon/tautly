data class Model(
    val circles: List<Circle<World>> = listOf(),
    val cursorPos: Vec2<Screen> = Vec2(0.0 to 0.0),
    val shouldClose: Boolean = false,
    val mousePressed: Map<Int, Boolean> = mapOf(),
    val zoom: Double = 1.0,
    val offset: Vec2<World> = Vec2(0.0 to 0.0),
    val windowSize: Pair<Int, Int> = 0 to 0,
    val palette: PaletteModel,
    val currentColour: Triple<Double, Double, Double>,
)

data class PaletteModel(
    val radius: Double,
    val separation: Double,
    val entries: List<Circle<Screen>> = listOf(),
)

data class Circle<S: Space>(
    val radius: Double,
    val position: Vec2<S>,
    val colour: Triple<Double, Double, Double>,
)


fun Model.toWorldSpace(pos: Vec2<Screen>): Vec2<World> {
    val (x, y) = (1 / zoom) * pos
    return Vec2<World>(x to y) - offset
}

fun Model.toScreenSpace(pos: Vec2<World>): Vec2<Screen> {
    val (x, y) = pos + offset
    return zoom * (Vec2(x to y))
}
