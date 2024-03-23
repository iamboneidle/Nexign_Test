package test_CDR;

import CDR.CDRObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Проверка для CDRObject
 */
public class CDRObjectTest {
    /**
     * Проверка правильности приведения к нужному формату строки полей объекта
     */
    @Test
    public void testToString() {
        CDRObject cdrObject = new CDRObject("01", "79123456789", 1234567890L, 9234567890L);
        assertEquals("01,79123456789,1234567890,9234567890", cdrObject.toString());
    }
}
