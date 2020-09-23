data class Model(
    val circles: List<Circle> = listOf(),
    val cursorPos: Pair<Double, Double> = .0 to .0,
    val shouldClose: Boolean = false,
    val mousePressed: Map<Int, Boolean> = mapOf<Int, Boolean>().withDefault { false },
    val zoom: Float = 1f,
    val offset: Pair<Float, Float> = 0f to 0f,
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
