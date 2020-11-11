import io.kotest.core.spec.style.StringSpec
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuadtreeTest : StringSpec( {
    "Quadtree.paint() should update the tree" {
        val tree = Leaf(Colour.black)
        val point = Vec2.world(3.0 / 4, 0.5)
        val tree2 = tree.paint(point, Vec2.world(1.0, 1.0), Leaf(Colour.white))

        assertEquals(Leaf(Colour.black), tree[point])
        assertEquals(Leaf(Colour.white), tree2[point])
        assertTrue(tree2.depth >= tree.depth)
    }

    ""
})
