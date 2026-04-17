package com.aafkir.tifssi.staffing.domain.enums;

/**
 * Status of a profile submission for a client need.
 */
public enum SubmissionStatus {
    /** Profile has been preselected internally. */
    PRESELECTED,
    /** Submission has been sent to the client. */
    SENT,
    /** Candidate is in interview process. */
    INTERVIEW,
    /** Candidate is shortlisted by the client. */
    SHORTLIST,
    /** Submission has been won. */
    WON,
    /** Submission has been lost. */
    LOST
}
