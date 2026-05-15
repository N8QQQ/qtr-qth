package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SimpleMockTest extends BddTest {
    @Test
    void testMock() {
        final ISerialPort mockPort = mock(ISerialPort.class);
        mockPort.openPort();
        verify(mockPort).openPort();
    }
}
