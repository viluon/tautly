data class Model(
    val circles: List<Triple<Float, Float, Float>> = listOf(),
    val shouldClose: Boolean = false,
    val mousePressed: Boolean = false,
)
