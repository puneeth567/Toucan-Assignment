package com.example.transactionstarter.model;

public enum TransactionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean canTransitionTo(TransactionStatus target) {
        if (target == null || target == this) {
            return true;
        }
        if (isTerminal()) {
            return false;
        }
        return switch (this) {
            case PENDING -> target == PROCESSING || target == COMPLETED || target == FAILED || target == CANCELLED;
            case PROCESSING -> target == COMPLETED || target == FAILED || target == CANCELLED;
            default -> false;
        };
    }
}
