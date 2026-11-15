package com.ratelimiter.shared.model;

/** Unified outcome status shared by {@link Response} (wire) and {@link RequestEvent} (analytics). */
public enum RequestStatus {
    SUCCESS,
    RATE_LIMITED
}
