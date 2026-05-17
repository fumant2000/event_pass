package com.eventpass.backend.service;

import com.eventpass.backend.dto.response.TransactionResponse;
import com.eventpass.backend.entity.User;
import com.eventpass.backend.repository.TransactionRepository;
import com.eventpass.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<TransactionResponse> getMyTransactions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return transactionRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(t -> TransactionResponse.builder()
                        .id(t.getId()).amount(t.getAmount()).type(t.getType())
                        .reference(t.getReference()).description(t.getDescription())
                        .createdAt(t.getCreatedAt())
                        .ticketId(t.getTicket() != null ? t.getTicket().getId() : null)
                        .eventTitle(t.getTicket() != null ? t.getTicket().getEvent().getTitle() : null)
                        .build())
                .collect(Collectors.toList());
    }
}