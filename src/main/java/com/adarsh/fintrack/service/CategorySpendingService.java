package com.adarsh.fintrack.service;

import com.adarsh.fintrack.dto.CategorySpendingResponse;
import com.adarsh.fintrack.entity.User;
import com.adarsh.fintrack.exception.ResourceNotFoundException;
import com.adarsh.fintrack.repository.CategorySpendingProjection;
import com.adarsh.fintrack.repository.TransactionRepository;
import com.adarsh.fintrack.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class CategorySpendingService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategorySpendingService(
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
    public List<CategorySpendingResponse> getCategorySpending(
            String month) {

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

        List<CategorySpendingProjection> spending =
                transactionRepository.findCategoryWiseSpending(
                        user.getId(),
                        startDate,
                        endDate
                );

        BigDecimal totalExpenses = spending.stream()
                .map(CategorySpendingProjection::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return List.of();
        }

        return spending.stream()
                .map(item -> {

                    BigDecimal percentage = item.getTotalAmount()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalExpenses, 2, RoundingMode.HALF_UP);

                    return new CategorySpendingResponse(
                            item.getCategoryId(),
                            item.getCategoryName(),
                            item.getTotalAmount(),
                            percentage
                    );
                })
                .toList();
    }
}