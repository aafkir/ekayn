package com.aafkir.tifssi.absences.domain.enums;

/**
 * Approval states of an absence request.
 */
public enum AbsenceStatus {
    /** Absence is still being prepared. */
    DRAFT,
    /** Absence has been submitted for approval. */
    SUBMITTED,
    /** Absence has been approved. */
    APPROVED,
    /** Absence has been rejected. */
    REJECTED
}
