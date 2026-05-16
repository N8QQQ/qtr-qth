package com.stoicprogrammer.qtrqth.base;

import org.mockito.BDDMockito;

/**
 * JARVIS-approved BDD Base Wrapper.
 * Provides stubbing helpers using BDDMockito.
 * Use direct AssertJ 'assertThat' for assertions.
 */
public class BddTest {

    /**
     * Use this for Mockito stubbing in a BDD style.
     */
    protected final <T> BDDMockito.BDDMyOngoingStubbing<T> givenStubbing(final T methodCall) {
        return BDDMockito.given(methodCall);
    }
}
