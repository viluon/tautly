import kotlin.math.pow
import kotlin.math.sqrt

sealed class Space
object World : Space()
object Screen : Space()

inline class Vec2<S : Space>(private val p: Pair<Double, Double>) {
    inline val magnitude: Double
        inline get() {
            val (x, y) = this
            return sqrt(x.pow(2) + y.pow(2))
        }

    operator fun component1() = p.first
    operator fun component2() = p.second

    operator fun plus(other: Vec2<S>): Vec2<S> = op(other) { x0, y0, x1, y1 ->
        x0 + x1 to y0 + y1
    }

    operator fun minus(other: Vec2<S>): Vec2<S> = op(other) { x0, y0, x1, y1 ->
        x0 - x1 to y0 - y1
    }

    operator fun div(other: Vec2<S>): Vec2<S> = op(other) { x0, y0, x1, y1 ->
        x0 / x1 to y0 / y1
    }

    operator fun times(other: Vec2<S>): Vec2<S> = op(other) { x0, y0, x1, y1 ->
        x0 * x1 to y0 * y1
    }

    private inline fun op(
        other: Vec2<S>,
        f: (x0: Double, y0: Double, x1: Double, y1: Double) -> Pair<Double, Double>,
    ): Vec2<S> {
        val (x0, y0) = this
        val (x1, y1) = other
        return Vec2(f(x0, y0, x1, y1))
    }
}

operator fun <S : Space> Double.times(pos: Vec2<S>): Vec2<S> {
    val (x, y) = pos
    return Vec2(this * x to this * y)
}
