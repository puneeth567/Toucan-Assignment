package com.example.transactionstarter;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.UpdateStatusRequest;
import com.example.transactionstarter.model.TransactionStatus;
import com.example.transactionstarter.model.TransactionType;
import com.example.transactionstarter.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("Test 1: Successful Transaction Creation")
    void test1_SuccessfulTransactionCreation() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TX-1001",
                "CUST-001",
                new BigDecimal("150.75"),
                "USD",
                TransactionType.PAYMENT,
                "Online Order Payment"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/transactions/TX-1001")))
                .andExpect(jsonPath("$.transactionId", is("TX-1001")))
                .andExpect(jsonPath("$.customerId", is("CUST-001")))
                .andExpect(jsonPath("$.amount", is(150.75)))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.transactionType", is("PAYMENT")))
                .andExpect(jsonPath("$.transactionStatus", is("PENDING")))
                .andExpect(jsonPath("$.description", is("Online Order Payment")))
                .andExpect(jsonPath("$.createdAt", notNullValue()));

        assertTrue(transactionRepository.existsByTransactionId("TX-1001"));
    }

    @Test
    @DisplayName("Test 2: Invalid Transaction Rejected (Negative Amount & Invalid Currency)")
    void test2_InvalidTransactionRejected() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                "TX-INVALID",
                "CUST-001",
                new BigDecimal("-50.00"),
                "INVALID",
                TransactionType.PAYMENT,
                "Invalid Payment"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.fieldErrors.amount", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.currency", notNullValue()));

        assertFalse(transactionRepository.existsByTransactionId("TX-INVALID"));
    }

    @Test
    @DisplayName("Test 3: Duplicate Transaction ID Rejected")
    void test3_DuplicateTransactionIdRejected() throws Exception {
        CreateTransactionRequest firstRequest = new CreateTransactionRequest(
                "TX-DUPLICATE",
                "CUST-001",
                new BigDecimal("100.00"),
                "USD",
                TransactionType.PAYMENT,
                "First Transaction"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        CreateTransactionRequest duplicateRequest = new CreateTransactionRequest(
                "TX-DUPLICATE",
                "CUST-002",
                new BigDecimal("200.00"),
                "EUR",
                TransactionType.DEPOSIT,
                "Duplicate Transaction Attempt"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("Transaction ID already exists: TX-DUPLICATE")));
    }

    @Test
    @DisplayName("Test 4: Non-existent Transaction Lookup Returns 404")
    void test4_NonExistentTransactionReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/TX-NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Transaction not found with ID: TX-NONEXISTENT")));
    }

    @Test
    @DisplayName("Test 5: Successful Transaction Status Update")
    void test5_SuccessfulStatusUpdate() throws Exception {
        CreateTransactionRequest createRequest = new CreateTransactionRequest(
                "TX-STATUS-1",
                "CUST-003",
                new BigDecimal("75.00"),
                "GBP",
                TransactionType.TRANSFER,
                "Status Test"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        UpdateStatusRequest statusRequest = new UpdateStatusRequest(TransactionStatus.PROCESSING);

        mockMvc.perform(patch("/api/v1/transactions/TX-STATUS-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionStatus", is("PROCESSING")));

        UpdateStatusRequest completeRequest = new UpdateStatusRequest(TransactionStatus.COMPLETED);

        mockMvc.perform(patch("/api/v1/transactions/TX-STATUS-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionStatus", is("COMPLETED")));
    }

    @Test
    @DisplayName("Test 6: Invalid Status Transition Rejected (Terminal State Modification)")
    void test6_InvalidStatusTransitionRejected() throws Exception {
        CreateTransactionRequest createRequest = new CreateTransactionRequest(
                "TX-TERMINAL",
                "CUST-004",
                new BigDecimal("500.00"),
                "EUR",
                TransactionType.PAYMENT,
                "Terminal State Test"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        UpdateStatusRequest completeRequest = new UpdateStatusRequest(TransactionStatus.COMPLETED);
        mockMvc.perform(patch("/api/v1/transactions/TX-TERMINAL/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk());

        UpdateStatusRequest cancelRequest = new UpdateStatusRequest(TransactionStatus.CANCELLED);
        mockMvc.perform(patch("/api/v1/transactions/TX-TERMINAL/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", containsString("Cannot transition transaction status from COMPLETED to CANCELLED")));
    }

    @Test
    @DisplayName("Test 7: Get Customer Transactions (Multiple Results)")
    void test7_GetCustomerTransactionsMultipleResults() throws Exception {
        CreateTransactionRequest tx1 = new CreateTransactionRequest("TX-CUST1-1", "CUST-MULTI", new BigDecimal("10.00"), "USD", TransactionType.PAYMENT, "Item 1");
        CreateTransactionRequest tx2 = new CreateTransactionRequest("TX-CUST1-2", "CUST-MULTI", new BigDecimal("20.00"), "USD", TransactionType.REFUND, "Item 2");
        CreateTransactionRequest tx3 = new CreateTransactionRequest("TX-OTHER-1", "CUST-OTHER", new BigDecimal("30.00"), "USD", TransactionType.PAYMENT, "Other Item");

        mockMvc.perform(post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tx1))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tx2))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tx3))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/customers/CUST-MULTI/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].transactionId", containsInAnyOrder("TX-CUST1-1", "TX-CUST1-2")));
    }

    @Test
    @DisplayName("Test 8: Get Customer Transactions (Empty Result)")
    void test8_GetCustomerTransactionsEmptyResult() throws Exception {
        mockMvc.perform(get("/api/v1/customers/CUST-UNKNOWN/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
