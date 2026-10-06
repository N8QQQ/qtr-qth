package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.then;

class SimpleMockTest extends BddTest {
    @Test
    void should_open_port_successfully_when_requested() {
        final ISerialPort mockPort = mock(ISerialPort.class);
        mockPort.openPort();
        then(mockPort).should().openPort();
    }
}
