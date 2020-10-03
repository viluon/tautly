import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

sealed class Space
object World : Space()
object Screen : Space()

@Suppress("DataClassPrivateConstructor")
data class Vec2<S : Space> private constructor(val x: Double, val y: Double) {
    companion object {
        fun screen(a: Double, b: Double): Vec2<Screen> = Vec2(a, b)

        fun world(a: Double, b: Double): Vec2<World> = when {
            abs(a) <= 1 && abs(b) <= 1 -> Vec2(a, b)
            else -> throw IllegalArgumentException("Vec2<World> components must be values between -1.0 and 1.0 (got $a, $b)")
        }

        fun world(p: Pair<Double, Double>): Vec2<World> = world(p.first, p.second)
    }

    inline val magnitude: Double inline get() = sqrt(x.pow(2) + y.pow(2))

    inline val abs: Vec2<S> inline get() = copy(x = abs(x), y = abs(y))

    private fun Double.round(n: Int): Double {
        val exp = 10.0.pow(n)
        return floor(this * exp + 0.5) / exp
    }

    fun round(n: Int): Vec2<S> = copy(x = x.round(n), y = y.round(n))

    operator fun plus(other: Vec2<S>): Vec2<S> = op(other) { x0, y0, x1, y1 ->
        Vec2(x0 + x1, y0 + y1)
    }

    operator fun minus(other: Vec2<S>): Vec2<S> = op(other) { x0, y0, x1, y1 ->
        Vec2(x0 - x1, y0 - y1)
    }

    operator fun div(other: Vec2<S>): Vec2<S> = op(other) { x0, y0, x1, y1 ->
        Vec2(x0 / x1, y0 / y1)
    }

    operator fun times(other: Vec2<S>): Vec2<S> = op(other) { x0, y0, x1, y1 ->
        Vec2(x0 * x1, y0 * y1)
    }

    private inline fun op(other: Vec2<S>, f: (x0: Double, y0: Double, x1: Double, y1: Double) -> Vec2<S>): Vec2<S> =
        f(x, y, other.x, other.y)
}

operator fun <S : Space> Double.times(pos: Vec2<S>): Vec2<S> {
    val (x, y) = pos
    return pos.copy(x = this * x, y = this * y)
}
