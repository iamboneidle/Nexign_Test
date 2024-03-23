package test_UDR;

import UDR.IncomingCall;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Проверка для IncomingCall
 */
public class IncomingCallTest {
    /**
     * Проверка правильности работы приведения totalTime к strTotalTime
     */
    @Test
    public void testGetStrTotalTime() {
        IncomingCall incomingCall = new IncomingCall(1711220978433L, 1711218261433L);
        assertEquals("02:14:43", incomingCall.getStrTotalTime());
    }
}
