package com.aafkir.tifssi.expenses.domain.enums;

/**
 * Validation states of an expense.
 */
public enum ExpenseStatus {
    /** Expense is still editable. */
    DRAFT,
    /** Expense has been validated. */
    VALIDATED,
    /** Expense has been rejected. */
    REJECTED
}
