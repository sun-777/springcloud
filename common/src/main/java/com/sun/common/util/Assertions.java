package com.sun.common.util;

public final class Assertions {

    public static <T> T notNull(final String name, final T value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " can not be null");
        }
        return value;
    }


    public static void isTrueArgument(final String name, final boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("state should be: " + name);
        }
    }

    public static <T> T isTrueArgument(final String name, final T value, final boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("state should be: " + name);
        }
        return value;
    }



    private Assertions() {
        throw new IllegalStateException("Instantiation not allowed");
    }
}
