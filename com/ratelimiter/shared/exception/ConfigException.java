package com.ratelimiter.shared.exception;

/** Thrown when a configuration object is built with invalid values. */
public class ConfigException extends RuntimeException {
    public ConfigException(String message) { super(message); }
}
