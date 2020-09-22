
infix fun <A, B> Pair<A, A>.map(f: (A) -> B): Pair<B, B> = f(first) to f(second)
