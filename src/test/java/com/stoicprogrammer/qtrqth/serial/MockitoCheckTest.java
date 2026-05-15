package com.stoicprogrammer.qtrqth.serial;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MockitoCheckTest {
    @Test
    void testListMock() {
        final List mockList = mock(List.class);
        when(mockList.size()).thenReturn(10);
        assertEquals(10, mockList.size());
    }
}
