sealed class Quadtree {
    abstract val colour: Colour
    abstract val depth: Int
}

data class Node(val children: Array<Quadtree>) : Quadtree() {
    // the colour of a node is the average of the inner colours
    override val colour: Colour = children.map { it.colour }.foldRight(Colour(0.0, 0.0, 0.0, 0.0)) { c, acc ->
        c + acc
    } / children.size.toDouble()

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

fun Quadtree.paint(point: Vec2<World>, resolution: Vec2<World>, stroke: Quadtree): Quadtree {
    val (resX, resY) = resolution
    return when {
        resX >= 1.0 && resY >= 1.0 -> stroke
        this is Leaf -> Node(many(4).toTypedArray()).paint(point, resolution, stroke)
        else -> { // this is Node
            this as Node
            val (index, newPoint) = point.translateForChild()

            updateChild(index, children[index].paint(newPoint, 2.0 * resolution, stroke))
        }
    }
}

private fun Vec2<World>.translateForChild(): Pair<Int, Vec2<World>> = when {
    x < 0 && y < 0 -> 0 to 2.0 * this + Vec2.world(1.0, 1.0)
    y < 0 -> 1 to 2.0 * this + Vec2.world(-1.0, 1.0)
    x < 0 -> 2 to 2.0 * this + Vec2.world(1.0, -1.0)
    else -> 3 to 2.0 * this + Vec2.world(-1.0, -1.0)
}

fun Node.updateChild(n: Int, child: Quadtree): Node = Node(children.copyOf().also { it[n] = child })

operator fun Quadtree.get(point: Vec2<World>): Leaf = foldAt(point, null as Leaf?) { quad, _ ->
    when (quad) {
        is Leaf -> quad
        else -> null
    }
}!!

operator fun Quadtree.set(point: Vec2<World>, size: Vec2<World>, leaf: Leaf): Quadtree {
    return if (size == Vec2.world(0.0, 0.0)) this else paint(point, size, leaf)
}

//fun Quadtree.paint(point: Vec2<World>, size: Vec2<World>, leaf: Leaf): Quadtree {
//    val (sizeX, sizeY) = size.abs
//    return when {
//        sizeX >= 1.0 && sizeY >= 1.0 -> leaf
//        this is Leaf -> TODO()
//        this is Node -> TODO()
//        else -> throw IllegalStateException()
//    }
//}

/*
tailrec operator fun Quadtree.get(point: Vec2<World>): Leaf = when (this) {
    is Leaf -> this
    is Node -> {
        val (x, y) = point
        // TODO there should be a stopping condition based on the required resolution
        //      to avoid recursing into sub-pixel nodes
        when {
            x < 0 && y < 0 -> children[0][2.0 * point + Vec2(1.0 to 1.0)]
            y < 0 -> children[1][2.0 * point + Vec2(-1.0 to 1.0)]
            x < 0 -> children[2][2.0 * point + Vec2(1.0 to -1.0)]
            else -> children[3][2.0 * point + Vec2(-1.0 to -1.0)]
        }
    }
}

tailrec operator fun Quadtree.set(point: Vec2<World>, leaf: Leaf): Quadtree = when (this) {
    // TODO there should be a "starting condition" based on the current brush size
    //      to increase the resolution of the tree (recurse and create children until the brush
    //      is large enough?)
    is Leaf -> leaf
    is Node -> {
        val (x, y) = point
        when {
            // FIXME this should combine modified children into new nodes
            x < 0 && y < 0 -> children[0].set(2.0 * point + Vec2(1.0 to 1.0), leaf)
            y < 0 -> children[1].set(2.0 * point + Vec2(-1.0 to 1.0), leaf)
            x < 0 -> children[2].set(2.0 * point + Vec2(1.0 to -1.0), leaf)
            else -> children[3].set(2.0 * point + Vec2(-1.0 to -1.0), leaf)
        }
    }
}
// */
