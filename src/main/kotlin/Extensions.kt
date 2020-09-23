inline infix fun <A, B> Pair<A, A>.map(f: (A) -> B): Pair<B, B> = f(first) to f(second)

operator fun Pair<Float, Float>.plus(other: Pair<Float, Float>): Pair<Float, Float> =
    first + other.first to second + other.second

operator fun Pair<Float, Float>.minus(other: Pair<Float, Float>): Pair<Float, Float> =
    first - other.first to second - other.second

operator fun Pair<Float, Float>.div(other: Pair<Float, Float>): Pair<Float, Float> =
    first / other.first to second / other.second

operator fun Float.times(p: Pair<Float, Float>): Pair<Float, Float> = this * p.first to this * p.second
