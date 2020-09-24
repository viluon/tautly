import kotlin.math.pow
import kotlin.math.sqrt

inline class ScreenSpace(private val p: Pair<Double, Double>) {
    inline val magnitude: Double
        inline get() {
            val (x, y) = this
            return sqrt(x.pow(2) + y.pow(2))
        }

    operator fun component1() = p.first
    operator fun component2() = p.second

    operator fun plus(other: ScreenSpace): ScreenSpace = op(other) { x0, y0, x1, y1 ->
        x0 + x1 to y0 + y1
    }

    operator fun minus(other: ScreenSpace): ScreenSpace = op(other) { x0, y0, x1, y1 ->
        x0 - x1 to y0 - y1
    }

    operator fun div(other: ScreenSpace): ScreenSpace = op(other) { x0, y0, x1, y1 ->
        x0 / x1 to y0 / y1
    }

    operator fun times(other: ScreenSpace): ScreenSpace = op(other) { x0, y0, x1, y1 ->
        x0 * x1 to y0 * y1
    }

    private inline fun op(
        other: ScreenSpace,
        f: (x0: Double, y0: Double, x1: Double, y1: Double) -> Pair<Double, Double>,
    ): ScreenSpace {
        val (x0, y0) = this
        val (x1, y1) = other
        return ScreenSpace(f(x0, y0, x1, y1))
    }
}

inline class WorldSpace(private val p: Pair<Double, Double>) {
    inline val magnitude: Double
        inline get() {
            val (x, y) = this
            return sqrt(x.pow(2) + y.pow(2))
        }

    operator fun component1() = p.first
    operator fun component2() = p.second

    operator fun plus(other: WorldSpace): WorldSpace = op(other) { x0, y0, x1, y1 ->
        x0 + x1 to y0 + y1
    }

    operator fun minus(other: WorldSpace): WorldSpace = op(other) { x0, y0, x1, y1 ->
        x0 - x1 to y0 - y1
    }

    operator fun div(other: WorldSpace): WorldSpace = op(other) { x0, y0, x1, y1 ->
        x0 / x1 to y0 / y1
    }

    operator fun times(other: WorldSpace): WorldSpace = op(other) { x0, y0, x1, y1 ->
        x0 * x1 to y0 * y1
    }

    private inline fun op(
        other: WorldSpace,
        f: (x0: Double, y0: Double, x1: Double, y1: Double) -> Pair<Double, Double>,
    ): WorldSpace {
        val (x0, y0) = this
        val (x1, y1) = other
        return WorldSpace(f(x0, y0, x1, y1))
    }
}

operator fun Double.times(pos: ScreenSpace): ScreenSpace {
    val (x, y) = pos
    return ScreenSpace(this * x to this * y)
}

operator fun Double.times(pos: WorldSpace): WorldSpace {
    val (x, y) = pos
    return WorldSpace(this * x to this * y)
}
