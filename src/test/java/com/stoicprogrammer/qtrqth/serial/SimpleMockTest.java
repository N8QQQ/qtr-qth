package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleMockTest {
    @Test
    void testSimpleMock() {
        ISerialPort mockPort = mock(ISerialPort.class);
        when(mockPort.getSystemPortName()).thenReturn("COM1");
        assertEquals("COM1", mockPort.getSystemPortName());
    }
}
