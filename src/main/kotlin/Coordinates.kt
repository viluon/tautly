import kotlin.math.*

sealed class Space
object World : Space()
object Screen : Space()

@Suppress("DataClassPrivateConstructor")
data class Vec2<S : Space> private constructor(val x: Double, val y: Double) {
    companion object {
        fun <S : Space> zero(): Vec2<S> = Vec2(0.0, 0.0)

        fun screen(a: Double, b: Double): Vec2<Screen> = Vec2(a, b)
        fun screen(p: Pair<Double, Double>): Vec2<Screen> = screen(p.first, p.second)

        fun world(a: Double, b: Double): Vec2<World> = Vec2(a, b)
    }

    inline val magnitude: Double inline get() = sqrt(x.pow(2) + y.pow(2))
    inline val abs: Vec2<S> inline get() = copy(x = abs(x), y = abs(y))
    inline val max: Double inline get() = max(x, y)
    inline val min: Double inline get() = min(x, y)

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

    operator fun unaryMinus(): Vec2<S> = copy(x = -x, y = -y)
}

operator fun <S : Space> Double.times(pos: Vec2<S>): Vec2<S> {
    val (x, y) = pos
    return pos.copy(x = this * x, y = this * y)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <S: Space> Double.vec(): Vec2<S> = Vec2.zero<S>().copy(this, this)
