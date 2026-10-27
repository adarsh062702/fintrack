
package com.adarsh.fintrack.service;

import com.adarsh.fintrack.dto.MonthlySummaryResponse;
import com.adarsh.fintrack.entity.Transaction;
import com.adarsh.fintrack.entity.User;
import com.adarsh.fintrack.exception.ResourceNotFoundException;
import com.adarsh.fintrack.repository.TransactionRepository;
import com.adarsh.fintrack.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Service
public class MonthlySummaryService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public MonthlySummaryService(
            TransactionRepository transactionRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException(
                    "Authentication is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"));
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(String month) {
        YearMonth yearMonth;

        try {
            yearMonth = YearMonth.parse(month);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "Month must be in YYYY-MM format, for example 2026-10");
        }

        User user = getAuthenticatedUser();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions =
                transactionRepository
                        .findByUserIdAndTransactionDateBetween(
                                user.getId(),
                                startDate,
                                endDate
                        );

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            String type = transaction.getType() == null
                    ? ""
                    : transaction.getType().trim()
                    .toUpperCase(Locale.ROOT);

            if ("INCOME".equals(type)) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else if ("EXPENSE".equals(type)) {
                totalExpenses =
                        totalExpenses.add(transaction.getAmount());
            }
        }

        BigDecimal savings = totalIncome.subtract(totalExpenses);

        return new MonthlySummaryResponse(
                yearMonth.toString(),
                totalIncome,
                totalExpenses,
                savings,
                transactions.size()
        );
    }
}
