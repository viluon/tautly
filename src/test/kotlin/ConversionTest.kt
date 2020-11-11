import Canvas.Companion.unsigned_char
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlin.test.assertEquals

class ConversionTest : StringSpec({
    "Java bytes should be correctly converted to unsigned chars" {
        assertEquals(-1, unsigned_char(1f))
        assertEquals(0, unsigned_char(0f))
    }

    "the conversion from screen space to world space and back again shouldn't change the coords" {
        val model = Model(
            offset = Vec2.world(0.1, 0.27),
            zoom = 3.0,
            currentColour = Colour.white,
            palette = PaletteModel(1.0, 1.0),
            world = Leaf(Colour.black),
            windowSize = Vec2.screen(640.0, 480.0)
        )

        val ss = Vec2.screen(121.0, 43.0)

        assertEquals(ss, model.toScreenSpace(model.toWorldSpace(ss)).round(5))
    }

    "screen space -- world space conversion should work regardless of zoom and offset" {
        checkAll(50_000, PropTestConfig(seed = 42), arbitrary { rs ->
            fun Arb<Int>.sampleToDouble(): Double = sample(rs).value.toDouble()

            val model = Arb.primitiveModel().sample(rs).value
            val (winW, winH) = model.windowSize

            model to Vec2.screen(
                Arb.int(0..winW.toInt()).sampleToDouble(),
                Arb.int(0..winH.toInt()).sampleToDouble(),
            )
        }) { (model, ss) ->
            assertEquals(ss, model.toScreenSpace(model.toWorldSpace(ss)).round(5))
        }
    }
})
