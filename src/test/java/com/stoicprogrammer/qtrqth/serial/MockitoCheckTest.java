package com.stoicprogrammer.qtrqth.serial;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

class MockitoCheckTest {
    @Test
    void should_return_mocked_size_when_list_is_queried() {
        final List<?> mockList = mock(List.class);
        given(mockList.size()).willReturn(10);
        assertThat(mockList.size()).isEqualTo(10);
    }
}
