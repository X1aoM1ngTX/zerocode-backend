package com.xm.vexorbackend.core.builder;

import java.util.concurrent.Semaphore;

final class NpmBuildConcurrencyLimiter {

    private static final int DEFAULT_MAX_CONCURRENCY = 1;

    static final Semaphore SEMAPHORE = new Semaphore(
            Integer.getInteger("vexor.npm.build.max-concurrency", DEFAULT_MAX_CONCURRENCY),
            true
    );

    private NpmBuildConcurrencyLimiter() {
    }
}
