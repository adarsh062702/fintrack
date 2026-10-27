
package com.adarsh.fintrack.service;

import com.adarsh.fintrack.dto.CreateTransactionRequest;
import com.adarsh.fintrack.dto.TransactionResponse;
import com.adarsh.fintrack.entity.Category;
import com.adarsh.fintrack.entity.Transaction;
import com.adarsh.fintrack.entity.User;
import com.adarsh.fintrack.exception.ResourceNotFoundException;
import com.adarsh.fintrack.repository.CategoryRepository;
import com.adarsh.fintrack.repository.TransactionRepository;
import com.adarsh.fintrack.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
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

    private Category getOwnedCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + categoryId));

        if (category.getUser() == null
                || !category.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot use another user's category");
        }

        if (!category.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot use an inactive category");
        }

        return category;
    }

    private void validateTransactionType(String transactionType,
                                         Category category) {
        if (transactionType == null || transactionType.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction type is required");
        }

        String normalizedType =
                transactionType.trim().toUpperCase(Locale.ROOT);
        String categoryType =
                category.getType() == null
                        ? ""
                        : category.getType().trim().toUpperCase(Locale.ROOT);

        if (!normalizedType.equals("INCOME")
                && !normalizedType.equals("EXPENSE")) {
            throw new IllegalArgumentException(
                    "Transaction type must be INCOME or EXPENSE");
        }

        if (!normalizedType.equals(categoryType)) {
            throw new IllegalArgumentException(
                    "Transaction type must match the category type: "
                            + categoryType);
        }
    }

    private TransactionResponse toResponse(Transaction transaction) {
        Category category = transaction.getCategory();

        return new TransactionResponse(
                transaction.getId(),
                category.getId(),
                category.getName(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getPaymentMethod(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        User user = getAuthenticatedUser();

        return transactionRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        User user = getAuthenticatedUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction not found with id: " + id));

        if (transaction.getUser() == null
                || !transaction.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot access another user's transaction");
        }

        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponse createTransaction(
            CreateTransactionRequest request) {
        User user = getAuthenticatedUser();

        Category category =
                getOwnedCategory(request.getCategoryId(), user);

        validateTransactionType(request.getType(), category);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setType(
                request.getType().trim().toUpperCase(Locale.ROOT));
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(
                request.getPaymentMethod().trim());

        Transaction saved = transactionRepository.save(transaction);

        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse updateTransaction(
            Long id, CreateTransactionRequest request) {
        User user = getAuthenticatedUser();

        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction not found with id: " + id));

        if (existing.getUser() == null
                || !existing.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot update another user's transaction");
        }

        Category category =
                getOwnedCategory(request.getCategoryId(), user);

        validateTransactionType(request.getType(), category);

        existing.setCategory(category);
        existing.setType(
                request.getType().trim().toUpperCase(Locale.ROOT));
        existing.setAmount(request.getAmount());
        existing.setDescription(request.getDescription());
        existing.setTransactionDate(request.getTransactionDate());
        existing.setPaymentMethod(request.getPaymentMethod().trim());

        Transaction saved = transactionRepository.save(existing);

        return toResponse(saved);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        User user = getAuthenticatedUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction not found with id: " + id));

        if (transaction.getUser() == null
                || !transaction.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot delete another user's transaction");
        }

        transactionRepository.delete(transaction);
    }
}
