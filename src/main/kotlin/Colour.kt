data class Colour(val hue: Double, val saturation: Double, val lightness: Double, val alpha: Double = 1.0) {
    companion object {
        val black = Colour(0.0, 0.0, 0.05)
        val white = Colour(0.0, 0.0, 0.95)
        val greenish = Colour(159 / 360.0, 0.74, 0.54)
        val purple = Colour(268 / 360.0, 0.74, 0.54)
        val lightGreen = Colour(68 / 360.0, .74, .54)
        val lightBlue = Colour(191 / 360.0, .74, .54)
        val magenta = Colour(323 / 360.0, .74, .54)
        val orange = Colour(12 / 360.0, .74, .54)
    }

    inline val inverted: Colour inline get() = Colour(1 - hue, saturation, 1 - lightness, alpha)

    operator fun plus(other: Colour): Colour =
        Colour(hue + other.hue, saturation + other.saturation, lightness + other.lightness, alpha + other.alpha)
}

operator fun Colour.div(d: Double) = Colour(hue / d, saturation / d, lightness / d, alpha / d)
