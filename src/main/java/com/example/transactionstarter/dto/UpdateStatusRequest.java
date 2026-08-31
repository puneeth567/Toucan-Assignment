package com.example.transactionstarter.dto;

import com.example.transactionstarter.model.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Transaction status is required")
    private TransactionStatus status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(TransactionStatus status) {
        this.status = status;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
