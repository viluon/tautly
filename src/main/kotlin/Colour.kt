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

    inline val inverted: Colour inline get() = Colour(1 - hue, saturation, 1 - lightness, alpha)

    operator fun plus(other: Colour): Colour =
        Colour(hue + other.hue, saturation + other.saturation, lightness + other.lightness, alpha + other.alpha)
}

operator fun Colour.div(d: Double) = Colour(hue / d, saturation / d, lightness / d, alpha / d)
