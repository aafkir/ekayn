package com.aafkir.tifssi.projects.domain.enums;

/**
 * Lifecycle states of a mission assignment.
 */
public enum MissionStatus {
    /** Mission is planned but not started. */
    PLANNED,
    /** Mission is currently active. */
    ACTIVE,
    /** Mission has ended normally. */
    ENDED,
    /** Mission has been cancelled. */
    CANCELLED
}
