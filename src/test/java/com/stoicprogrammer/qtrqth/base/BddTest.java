package com.stoicprogrammer.qtrqth.base;

import org.mockito.BDDMockito;
import org.junit.jupiter.api.Assertions;

/**
 * JARVIS-approved BDD Base Wrapper.
 * Provides assertion and stubbing helpers while avoiding name clashes.
 */
public class BddTest {

    /**
     * Use this for Mockito stubbing in a BDD style.
     */
    protected <T> BDDMockito.BDDMyOngoingStubbing<T> givenStubbing(final T methodCall) {
        return BDDMockito.given(methodCall);
    }

    /**
     * Assertion helper for general objects.
     */
    protected <T> void then(final T actual, final T expected) {
        Assertions.assertEquals(expected, actual);
    }

    /**
     * Assertion helper for doubles with precision delta.
     */
    protected void then(final double actual, final double expected) {
        Assertions.assertEquals(expected, actual, 0.000001);
    }

    /**
     * Assertion helper for booleans.
     */
    protected void thenTrue(final boolean condition) {
        Assertions.assertTrue(condition);
    }

    /**
     * Assertion helper for nullity.
     */
    protected void thenNotNull(final Object object) {
        Assertions.assertNotNull(object);
    }
}
