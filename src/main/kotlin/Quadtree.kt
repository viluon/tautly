import kotlin.math.log2
import kotlin.math.roundToInt

sealed class Quadtree {
    abstract val colour: Colour
    abstract val depth: Int
}

data class Node(val children: Array<Quadtree>) : Quadtree() {
    // the colour of a node is the average of the inner colours
    override val colour: Colour = children.map { it.colour }.mix()

    override val depth: Int = children.maxOf { it.depth } + 1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node
        if (!children.contentEquals(other.children)) return false
        return colour == other.colour
    }

    override fun hashCode(): Int = 31 * children.contentHashCode() + colour.hashCode()
}

data class Leaf(override val colour: Colour) : Quadtree() {
    override val depth: Int get() = 1
}

tailrec fun <A> Quadtree.foldAt(point: Vec2<World>, init: A, f: (Quadtree, A) -> A): A = when (this) {
    is Leaf -> f(this, init)
    is Node -> {
        val (x, y) = point
        val next = f(this, init)
        // TODO there should be a stopping condition based on the required resolution
        //      to avoid recursing into sub-pixel nodes
        when {
            x < 0 && y < 0 -> children[0].foldAt(2.0 * point + Vec2.world(1.0, 1.0), next, f)
            y < 0 -> children[1].foldAt(2.0 * point + Vec2.world(-1.0, 1.0), next, f)
            x < 0 -> children[2].foldAt(2.0 * point + Vec2.world(1.0, -1.0), next, f)
            else -> children[3].foldAt(2.0 * point + Vec2.world(-1.0, -1.0), next, f)
        }
    }
}

fun Model.paint(point: Vec2<World>, resolution: Vec2<World>, stroke: Quadtree) =
    world.paintUpwards(point, resolution, stroke, camera, Leaf(Colour.darkGrayishBlue)).let { (camera, qt) ->
        copy(world = qt) + camera
    }

tailrec fun Quadtree.paintUpwards(
    point: Vec2<World>,
    resolution: Vec2<World>,
    stroke: Quadtree,
    cam: CameraModel,
    background: Quadtree,
): Pair<CameraModel, Quadtree> = when {
    point.abs.max > 1.0 -> {
        val (index, origin, newPoint) = point.translateForParent()
        val newZoomLvl: Int = (1.0 / log2(CameraModel.zoomBase) + cam.zoomLevel).roundToInt()
        val newModel =
            cam.copy(zoomLevel = newZoomLvl, offset = cam.calculateZoomOffset(newZoomLvl, cam.toScreenSpace(-origin)))
        Node(background.many(4).toTypedArray())
            .updateChild(index, this)
            .paintUpwards(newPoint, 0.5 * resolution, stroke, newModel, background)
    }
    else -> cam to paintDownwards(point, resolution, stroke)
}

fun Quadtree.paintDownwards(point: Vec2<World>, resolution: Vec2<World>, stroke: Quadtree): Quadtree = when {
    resolution.min >= 1.0 -> stroke
    this is Leaf -> Node(many(4).toTypedArray()).paintDownwards(point, resolution, stroke)
    else -> { // this is Node
        this as Node
        val (index, newPoint) = point.translateForChild()

        updateChild(index, children[index].paintDownwards(newPoint, 2.0 * resolution, stroke))
    }
}

private fun Vec2<World>.translateForChild(): Pair<Int, Vec2<World>> = (2.0 * this).let {
    when {
        x < 0 && y < 0 -> 0 to it + Vec2.world(1.0, 1.0)
        y < 0 -> 1 to it + Vec2.world(-1.0, 1.0)
        x < 0 -> 2 to it + Vec2.world(1.0, -1.0)
        else -> 3 to it + Vec2.world(-1.0, -1.0)
    }
}

fun Vec2<World>.translateForParent(): Triple<Int, Vec2<World>, Vec2<World>> {
    /*
       -1   1
      A | B | C
     ---+---+--- -1
      D | - | E
     ---+---+---  1
      F | G | H

     there are two options for D & E and B & G,
     pick the least index in each case
     */
    val (i, _) = when {
        x < -1 && y < -1 -> 3 to 0.5 * this // A
        x < -1 && y <  1 -> 1 to copy(x = 0.5 * x) // D
        x < -1 && y >= 1 -> 1 to copy() // F
        x <  1 && y < -1 -> 2 to copy() // B
        x <  1 && y >= 1 -> 0 to copy() // G
        x >= 1 && y < -1 -> 2 to copy() // C
        x >= 1 && y <  1 -> 0 to copy() // E
        x >= 1 && y >= 1 -> 0 to copy() // H
        else -> throw IllegalStateException()
    }

    val newOrigin = when (i) {
        0 -> Vec2.world(1.0, 1.0)
        1 -> Vec2.world(-1.0, 1.0)
        2 -> Vec2.world(1.0, -1.0)
        3 -> Vec2.world(-1.0, -1.0)
        else -> throw IllegalStateException()
    }

    return Triple(i, newOrigin, 0.5 * (this - newOrigin))
}

fun Node.updateChild(n: Int, child: Quadtree): Quadtree = Node(children.copyOf().also { it[n] = child }).consolidate()

fun Node.consolidate(): Quadtree = if (children.all { it is Leaf } && equalColours()) children[0] else this

@Suppress("NOTHING_TO_INLINE", "SimplifyBooleanWithConstants")
inline fun Node.equalColours(): Boolean = true
        && children[0].colour == children[1].colour
        && children[1].colour == children[2].colour
        && children[2].colour == children[3].colour

operator fun Quadtree.get(point: Vec2<World>): Leaf = foldAt(point, null as Leaf?) { quad, _ ->
    when (quad) {
        is Leaf -> quad
        else -> null
    }
}!!
