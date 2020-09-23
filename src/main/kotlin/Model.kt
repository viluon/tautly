data class Model(
    val circles: List<Circle<WorldSpace>> = listOf(),
    val cursorPos: ScreenSpace = ScreenSpace(0.0 to 0.0),
    val shouldClose: Boolean = false,
    val mousePressed: Map<Int, Boolean> = mapOf(),
    val zoom: Double = 1.0,
    val offset: ScreenSpace = ScreenSpace(0.0 to 0.0),
    val windowSize: Pair<Int, Int> = 0 to 0,
    val palette: PaletteModel,
    val currentColour: Triple<Double, Double, Double>,
)

data class PaletteModel(
    val radius: Double,
    val separation: Double,
    val entries: List<Circle<ScreenSpace>> = listOf(),
)

data class Circle<P>(
    val radius: Double,
    val position: P,
    val colour: Triple<Double, Double, Double>,
)


fun Model.toWorldSpace(pos: ScreenSpace): WorldSpace {
    val (x, y) = (1 / zoom) * pos - offset
    return WorldSpace(x to y)
}

fun Model.toScreenSpace(pos: WorldSpace): ScreenSpace {
    val (x, y) = pos
    return zoom * (ScreenSpace(x to y) + offset)
}
