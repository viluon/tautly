data class Model(
    val circles: List<Circle> = listOf(),
    val cursorPos: Pair<Double, Double> = .0 to .0,
    val shouldClose: Boolean = false,
    val mousePressed: Map<Int, Boolean> = mapOf<Int, Boolean>().withDefault { false },
    val zoom: Double = 1.0,
    val offset: Pair<Double, Double> = .0 to .0,
    val windowSize: Pair<Int, Int> = 0 to 0,
    val palette: PaletteModel,
    val currentColour: Triple<Float, Float, Float>,
)

data class PaletteModel(
    val radius: Float,
    val separation: Float,
    val entries: List<Circle> = listOf(),
)

data class Circle(
    val radius: Float,
    val position: Pair<Float, Float>,
    val colour: Triple<Float, Float, Float>,
)
