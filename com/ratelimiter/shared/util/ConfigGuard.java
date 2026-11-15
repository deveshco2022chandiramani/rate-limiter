package com.ratelimiter.shared.util;

import com.ratelimiter.shared.exception.ConfigException;

/** Shared guard for Builder validation: throws {@link ConfigException} on failed conditions. */
public final class ConfigGuard {

    private ConfigGuard() {}

    public static void require(boolean condition, String message) {
        if (!condition) throw new ConfigException(message);
    }
}
