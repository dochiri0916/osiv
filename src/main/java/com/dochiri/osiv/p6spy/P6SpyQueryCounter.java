package com.dochiri.osiv.p6spy;

import java.util.concurrent.atomic.AtomicInteger;

public final class P6SpyQueryCounter {

    private static final AtomicInteger SQL_COUNT = new AtomicInteger();

    private P6SpyQueryCounter() {
    }

    public static void increment() {
        SQL_COUNT.incrementAndGet();
    }

    public static int get() {
        return SQL_COUNT.get();
    }

    public static void reset() {
        SQL_COUNT.set(0);
    }
}
