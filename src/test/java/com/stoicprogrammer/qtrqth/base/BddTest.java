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
    protected <T> BDDMockito.BDDMyOngoingStubbing<T> givenStubbing(T methodCall) {
        return BDDMockito.given(methodCall);
    }

    /**
     * Assertion helper for general objects.
     */
    protected <T> void then(T actual, T expected) {
        Assertions.assertEquals(expected, actual);
    }

    /**
     * Assertion helper for doubles with precision delta.
     */
    protected void then(double actual, double expected) {
        Assertions.assertEquals(expected, actual, 0.000001);
    }

    /**
     * Assertion helper for booleans.
     */
    protected void thenTrue(boolean condition) {
        Assertions.assertTrue(condition);
    }

    /**
     * Assertion helper for nullity.
     */
    protected void thenNotNull(Object object) {
        Assertions.assertNotNull(object);
    }
}
