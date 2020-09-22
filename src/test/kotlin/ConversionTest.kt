import Canvas.Companion.unsigned_char
import org.junit.Test
import kotlin.test.assertEquals

class ConversionTest {
    @Test
    fun testByteToChar() {
        assertEquals(-1, unsigned_char(1f))
        assertEquals(0, unsigned_char(0f))
    }
}
