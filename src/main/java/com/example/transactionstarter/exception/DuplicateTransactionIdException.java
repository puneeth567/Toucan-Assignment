package com.example.transactionstarter.exception;

public class DuplicateTransactionIdException extends RuntimeException {
    public DuplicateTransactionIdException(String message) {
        super(message);
    }
}
