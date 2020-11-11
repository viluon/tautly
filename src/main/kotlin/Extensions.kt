inline infix fun <A, B> Pair<A, A>.map(f: (A) -> B): Pair<B, B> = f(first) to f(second)

operator fun Pair<Double, Double>.plus(other: Pair<Double, Double>): Pair<Double, Double> =
    first + other.first to second + other.second

operator fun Pair<Double, Double>.minus(other: Pair<Double, Double>): Pair<Double, Double> =
    first - other.first to second - other.second

operator fun Pair<Double, Double>.div(other: Pair<Double, Double>): Pair<Double, Double> =
    first / other.first to second / other.second

operator fun Double.times(p: Pair<Double, Double>): Pair<Double, Double> = this * p.first to this * p.second

fun <A> A.many(n: Int): List<A> = when (n) {
    0 -> listOf()
    else -> this.many(n - 1) + this
}
