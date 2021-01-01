import kotlin.math.floor
import kotlin.math.pow

inline infix fun <A, B> Pair<A, A>.map(f: (A) -> B): Pair<B, B> = f(first) to f(second)

operator fun Pair<Double, Double>.plus(other: Pair<Double, Double>): Pair<Double, Double> =
    first + other.first to second + other.second

operator fun Pair<Double, Double>.minus(other: Pair<Double, Double>): Pair<Double, Double> =
    first - other.first to second - other.second

operator fun Pair<Double, Double>.div(other: Pair<Double, Double>): Pair<Double, Double> =
    first / other.first to second / other.second

operator fun Double.times(p: Pair<Double, Double>): Pair<Double, Double> = this * p.first to this * p.second

infix fun Double.round(n: Int): Double {
    val exp = 10.0.pow(n)
    return floor(this * exp + 0.5) / exp
}

fun <A> A.many(n: Int): List<A> = when (n) {
    0 -> listOf()
    else -> this.many(n - 1) + this
}

fun Colour.render(): String =
    Colour.namedColours.entries.firstOrNull { it.value === this }?.key ?: "HSLA($hue, $saturation, $lightness, $alpha)"

sealed class Either<out A, out B>()
data class Left<A>(val l: A) : Either<A, Nothing>()
data class Right<B>(val r: B) : Either<Nothing, B>()
