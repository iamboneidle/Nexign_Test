package test_UDR;

import UDR.OutcomingCall;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Проверка для OutcomingCall
 */
public class OutcomingCallTest {
    /**
     * Проверка правильности работы приведения totalTime к strTotalTime
     */
    @Test
    public void testGetStrTotalTime() {
        OutcomingCall outcomingCall = new OutcomingCall(1711220978433L, 1711218261433L);
        assertEquals("02:14:43", outcomingCall.getStrTotalTime());
    }
}
