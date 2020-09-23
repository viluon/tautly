import Canvas.Companion.unsigned_char
import org.junit.Test
import kotlin.test.assertEquals

class ConversionTest {
    @Test
    fun testByteToChar() {
        assertEquals(-1, unsigned_char(1f))
        assertEquals(0, unsigned_char(0f))
    }
    
    @Test
    fun testScreenSpaceWorldSpaceConversions() {
        val model = Model(
            offset = ScreenSpace(10.0 to 27.0),
            zoom = 3.0,
            currentColour = Triple(0.0, 0.0, 0.0),
            palette = PaletteModel(1.0, 1.0),
        )
        
        val ss = ScreenSpace(73.0 to 71.0)

        assertEquals(ss, model.toScreenSpace(model.toWorldSpace(ss)))
    }
}
