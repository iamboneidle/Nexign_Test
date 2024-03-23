package test_CDR;

import CDR.CDRGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Тесты для CDRGenerator
 */
public class CDRGeneratorTest {
    /**
     * Тест проверяет правильно ли расставлены границы Math.random() и номер не превысит по длине 11
     */
    @Test
    public void testPhoneNumbersGenerator() {
        for (int i = 0; i < 50; i++) {
            assertEquals(11, CDRGenerator.phoneNumbersGenerator().get((int) (Math.random() * 20)).length());
        }
    }

    /**
     * Тест проверят правильно ли расставлены границы Math.random(), так как должны возвращаться либо "01", либо "02"
     */
    @Test
    public void testCallTypeGenerator() {
        for (int i = 0; i < 50; i++) {
            String result = CDRGenerator.callTypeGenerator();
            assertTrue(result.equals("01") || result.equals("02"));
        }
    }
}
