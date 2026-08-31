package com.example.transactionstarter.dto;

import com.example.transactionstarter.model.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateTransactionRequest {

    @Size(max = 64, message = "Transaction ID must not exceed 64 characters")
    private String transactionId;

    @NotBlank(message = "Customer ID is required")
    @Size(max = 64, message = "Customer ID must not exceed 64 characters")
    private String customerId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 12, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO 4217 uppercase code")
    private String currency;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    public CreateTransactionRequest() {
    }

    public CreateTransactionRequest(String transactionId, String customerId, BigDecimal amount,
                                    String currency, TransactionType transactionType, String description) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
        this.description = description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
