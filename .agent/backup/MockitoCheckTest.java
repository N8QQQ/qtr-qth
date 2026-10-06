package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * Verification of Mockito environment.
 */
class MockitoCheckTest extends BddTest {

    private static final int LIST_SIZE = 10;

    @Test
    void should_verify_mockito_is_operational() {
        final List<String> mockList = Mockito.mock(List.class);
        Mockito.when(mockList.size()).thenReturn(LIST_SIZE);
        
        org.assertj.core.api.Assertions.assertThat(mockList.size()).isEqualTo(LIST_SIZE);
    }
}
