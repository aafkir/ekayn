package com.aafkir.tifssi.staffing.domain.enums;

/**
 * Lifecycle states of a client need.
 */
public enum NeedStatus {
    /** Need is being prepared internally. */
    DRAFT,
    /** Need is open and can receive candidates. */
    OPEN,
    /** Need has a reduced set of matching candidates. */
    SHORTLIST,
    /** Need has been presented to the client. */
    PRESENTED,
    /** Need has been won and can become a project. */
    WON,
    /** Need has been lost. */
    LOST
}
