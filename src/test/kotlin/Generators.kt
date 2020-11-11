import io.kotest.property.Arb
import io.kotest.property.Gen
import io.kotest.property.Shrinker
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.numericDoubles
import io.kotest.property.arbitrary.positiveDoubles
import io.kotest.property.arbitrary.*
import io.kotest.property.exhaustive.exhaustive
import org.lwjgl.glfw.GLFW
import kotlin.random.nextInt

fun Arb.Companion.primitiveModel(): Arb<Model> = Arb.bind(
    Arb.numericDoubles(-1.0, 1.0),
    Arb.numericDoubles(-1.0, 1.0),
    Arb.positiveDoubles(),
    Arb.positiveDoubles(),
    Arb.positiveDoubles(),
    Arb.quadtree(1),
) { offsetX, offsetY, zoom, windowWidth, windowHeight, leaf ->
    Model(
        offset = Vec2.world(offsetX, offsetY),
        zoom = zoom,
        windowSize = Vec2.screen(windowWidth, windowHeight),
        palette = PaletteModel(1.0, 1.0),
        currentColour = Colour.white,
        world = leaf,
    )
}

fun Arb.Companion.fullModel(depth: Int): Arb<Model> = Arb.bind(
    Arb.primitiveModel(),
    Arb.quadtree(depth),
) { model, tree -> model.copy(world = tree) }

val zeroToOne = Arb.numericDoubles(0.0, 1.0)
fun Arb.Companion.colour(): Arb<Colour> = Arb.bind(
    zeroToOne, zeroToOne, zeroToOne, zeroToOne,
) { h, s, l, a -> Colour(h, s, l, a) }

fun Arb.Companion.quadtree(depth: Int): Arb<Quadtree> = arbitrary(TreeShrinker) { rs ->
    when (depth) {
        1 -> Leaf(Arb.colour().sample(rs).value)
        else -> {
            val deep = rs.random.nextInt(1..4)
            val children = Arb
                .quadtree(depth - 1)
                .many(4)
                .mapIndexed { i, deepArb ->
                    val arb = if (i != deep && rs.random.nextDouble() < 0.5) Arb.quadtree(1)
                    else deepArb

                    arb.sample(rs).value
                }

            Node(children.toTypedArray())
        }
    }
}

object TreeShrinker : Shrinker<Quadtree> {
    override fun shrink(value: Quadtree): List<Quadtree> = when (value) {
        is Leaf -> listOf()
        is Node -> value.children.asList()
    }
}

fun GLFWAction.Companion.gen(): Gen<GLFWAction> = exhaustive(GLFWAction.values().asList())

fun CursorEvent.Companion.arb(): Arb<CursorEvent> = Arb.bind(
    Arb.positiveDoubles(), Arb.positiveDoubles(),
) { x, y -> CursorEvent(Vec2.screen(x, y)) }

fun KeyEvent.Companion.arb(): Arb<KeyEvent> = Arb.bind(
    Arb.int(0..1000), GLFWAction.gen()
) { key, action -> KeyEvent(key, GLFW.glfwGetKeyScancode(key), action, 0) }

fun MouseEvent.Companion.arb(): Arb<MouseEvent> = Arb.bind(
    Arb.int(0..5), GLFWAction.gen(),
) { button, action -> MouseEvent(button, action, 0) }

fun ScrollEvent.Companion.arb(): Arb<ScrollEvent> = Arb.bind(
    Arb.numericDoubles(-2.0, 2.0), Arb.numericDoubles(-2.0, 2.0),
) { vert, hor -> ScrollEvent(vert, hor) }

fun ResizeEvent.Companion.arb(): Arb<ResizeEvent> = Arb.bind(
    Arb.int(0..4000), Arb.int(0..4000),
) { w, h -> ResizeEvent(w, h) }

fun Arb.Companion.event(): Arb<Event> = Arb.choose(
    100 to CursorEvent.arb(),
    1 to KeyEvent.arb(),
    20 to MouseEvent.arb(),
    5 to ScrollEvent.arb(),
    1 to ResizeEvent.arb(),
)
