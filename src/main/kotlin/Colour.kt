import kotlin.math.*

data class Colour(val hue: Double, val saturation: Double, val lightness: Double, val alpha: Double = 1.0) {
    companion object {
        val namedColours: Map<String, Colour> = mapOf(
            "black" to Colour(0.0, 0.0, .05),
            "white" to Colour(0.0, 0.0, .95),
            "greenish" to Colour(159 / 360.0, .74, .54),
            "purple" to Colour(268 / 360.0, .74, .54),
            "lightGreen" to Colour(68 / 360.0, .74, .54),
            "lightBlue" to Colour(191 / 360.0, .74, .54),
            "magenta" to Colour(323 / 360.0, .74, .54),
            "orange" to Colour(12 / 360.0, .74, .54),
        )
        val black by namedColours
        val white by namedColours
        val greenish by namedColours
        val purple by namedColours
        val lightGreen by namedColours
        val lightBlue by namedColours
        val magenta by namedColours
        val orange by namedColours
    }

    inline val inverted: Colour inline get() = copy(hue = 1 - hue, lightness = 1 - lightness)

    operator fun plus(other: Colour): Colour =
        Colour(hue + other.hue, saturation + other.saturation, lightness + other.lightness, alpha + other.alpha)
}

// taken from https://stackoverflow.com/a/53328189
fun List<Colour>.mix(): Colour = fold(Triple(0.0, 0.0, 0.0)) { (x, y, z), (hue, saturation, lightness) ->
    Triple(
        x + cos(hue / 180 * PI) * saturation,
        y + sin(hue / 180 * PI) * saturation,
        z + lightness,
    )
}.let { (x, y, z) ->
    Triple(x / size, y / size, z / size)
}.let { (x, y, z) ->
    Colour(atan2(y, x) * 180 / PI, sqrt(x * x + y * y), z)
}
