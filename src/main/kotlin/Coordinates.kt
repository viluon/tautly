
inline class ScreenSpace(val p: Pair<Float, Float>) {
    fun component1() = p.first
    fun component2() = p.second
}
inline class WorldSpace(val p: Pair<Float, Float>) {
    fun component1() = p.first
    fun component2() = p.second
}
