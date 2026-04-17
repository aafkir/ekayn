package com.aafkir.tifssi.billing.domain.enums;

/**
 * Lifecycle states of an invoice.
 */
public enum InvoiceStatus {
    /** Invoice is still being prepared. */
    DRAFT,
    /** Invoice has been issued to the client. */
    ISSUED,
    /** Invoice has been paid. */
    PAID,
    /** Invoice has been cancelled. */
    CANCELLED
}
