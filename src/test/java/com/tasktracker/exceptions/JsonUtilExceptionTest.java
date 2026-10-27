package com.tasktracker.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JsonUtilExceptionTest {

    @Test
    void constructorPreservesMessageAndCause() {
        Throwable cause = new IllegalArgumentException("Root cause");
        JsonUtilException exception = new JsonUtilException("Failed to parse", cause);

        assertEquals("Failed to parse", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void constructorAllowsNullCause() {
        JsonUtilException exception = new JsonUtilException("Missing brace", null);

        assertEquals("Missing brace", exception.getMessage());
        assertNull(exception.getCause());
    }
}
