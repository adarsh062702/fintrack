
package com.adarsh.fintrack.service;

import com.adarsh.fintrack.dto.CategoryRequest;
import com.adarsh.fintrack.dto.CategoryResponse;
import com.adarsh.fintrack.entity.Category;
import com.adarsh.fintrack.entity.User;
import com.adarsh.fintrack.exception.ResourceNotFoundException;
import com.adarsh.fintrack.repository.CategoryRepository;
import com.adarsh.fintrack.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
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

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Category name is required");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() > 100) {
            throw new IllegalArgumentException(
                    "Category name cannot exceed 100 characters");
        }

        return trimmedName;
    }

    private String validateType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Category type is required");
        }

        String normalizedType = type.trim().toUpperCase(Locale.ROOT);

        if (!normalizedType.equals("INCOME")
                && !normalizedType.equals("EXPENSE")) {
            throw new IllegalArgumentException(
                    "Category type must be INCOME or EXPENSE");
        }

        return normalizedType;
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.isActive()
        );
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        User user = getAuthenticatedUser();

        return categoryRepository.findByUser_IdOrUserIsNull(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Category request is required");
        }

        User user = getAuthenticatedUser();

        Category category = new Category();
        category.setName(validateName(request.name()));
        category.setType(validateType(request.type()));
        category.setUser(user);
        category.setActive(true);

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(
            Long id, CategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Category request is required");
        }

        User user = getAuthenticatedUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + id));

        if (category.getUser() == null
                || !category.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot modify another user's category");
        }

        category.setName(validateName(request.name()));
        category.setType(validateType(request.type()));

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        User user = getAuthenticatedUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + id));

        if (category.getUser() == null
                || !category.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You cannot delete another user's category");
        }

        categoryRepository.delete(category);

    }
}
