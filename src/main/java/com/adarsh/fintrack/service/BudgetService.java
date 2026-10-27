
package com.adarsh.fintrack.service;

import com.adarsh.fintrack.dto.BudgetResponse;
import com.adarsh.fintrack.dto.CreateBudgetRequest;
import com.adarsh.fintrack.entity.Budget;
import com.adarsh.fintrack.entity.Category;
import com.adarsh.fintrack.entity.Transaction;
import com.adarsh.fintrack.entity.User;
import com.adarsh.fintrack.exception.ResourceNotFoundException;
import com.adarsh.fintrack.repository.BudgetRepository;
import com.adarsh.fintrack.repository.CategoryRepository;
import com.adarsh.fintrack.repository.TransactionRepository;
import com.adarsh.fintrack.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication is required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"));
    }

    private Category getOwnedExpenseCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + categoryId));

        if (category.getUser() == null
                || !category.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot use another user's category");
        }

        if (!category.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot use an inactive category");
        }

        if (!"EXPENSE".equalsIgnoreCase(category.getType())) {
            throw new IllegalArgumentException(
                    "Budgets can only be assigned to expense categories");
        }

        return category;
    }

    private BudgetResponse toResponse(Budget budget) {
        LocalDate start = LocalDate.of(
                budget.getYear(), budget.getMonth(), 1);

        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Transaction> transactions =
                transactionRepository.findByUserIdAndTransactionDateBetween(
                        budget.getUser().getId(), start, end);

        BigDecimal spent = transactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .filter(t -> t.getCategory() != null
                        && t.getCategory().getId()
                        .equals(budget.getCategory().getId()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = budget.getAmount().subtract(spent);

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getAmount(),
                spent,
                remaining,
                budget.getMonth(),
                budget.getYear()
        );
    }

    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request) {
        User user = getAuthenticatedUser();

        if (request.year() > LocalDate.now().getYear() + 10) {
            throw new IllegalArgumentException("Invalid budget year");
        }

        Category category =
                getOwnedExpenseCategory(request.categoryId(), user);

        boolean exists =
                budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                        user.getId(),
                        category.getId(),
                        request.month(),
                        request.year()
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "A budget already exists for this category and month");
        }

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmount(request.amount());
        budget.setMonth(request.month());
        budget.setYear(request.year());

        return toResponse(budgetRepository.save(budget));
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(Integer month, Integer year) {
        User user = getAuthenticatedUser();

        LocalDate today = LocalDate.now();
        int selectedMonth = month == null ? today.getMonthValue() : month;
        int selectedYear = year == null ? today.getYear() : year;

        if (selectedMonth < 1 || selectedMonth > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        return budgetRepository.findByUserIdAndMonthAndYear(
                        user.getId(), selectedMonth, selectedYear)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, BigDecimal amount) {
        User user = getAuthenticatedUser();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Budget amount must be greater than zero");
        }

        Budget budget = budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Budget not found: " + id));

        budget.setAmount(amount);

        return toResponse(budgetRepository.save(budget));
    }

    @Transactional
    public void deleteBudget(Long id) {
        User user = getAuthenticatedUser();

        Budget budget = budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Budget not found: " + id));

        budgetRepository.delete(budget);
    }
}
