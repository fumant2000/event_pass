package com.eventpass.backend.controller;

import com.eventpass.backend.dto.response.TransactionResponse;
import com.eventpass.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/history")
    public ResponseEntity<List<TransactionResponse>> getHistory(Principal principal) {
        return ResponseEntity.ok(transactionService.getMyTransactions(principal.getName()));
    }
}