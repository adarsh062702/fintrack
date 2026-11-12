
package com.adarsh.fintrack.service;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        user = mock(User.class);


        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==========================================
    // BUDGET CREATION TESTS
    // ==========================================

    @Test
    void createBudget_ShouldSucceed_WhenRequestIsValid() {

        Category category = mock(Category.class);

        when(user.getId()).thenReturn(1L);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getId()).thenReturn(10L);
        when(category.getName()).thenReturn("Food");
        when(category.getUser()).thenReturn(user);
        when(category.isActive()).thenReturn(true);
        when(category.getType()).thenReturn("EXPENSE");

        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                1L, 10L, 11, 2026
        )).thenReturn(false);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.findByUserIdAndTransactionDateBetween(
                eq(1L),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of());

        CreateBudgetRequest request = new CreateBudgetRequest(
                10L,
                new BigDecimal("10000.00"),
                11,
                2026
        );

        var response = budgetService.createBudget(request);

        assertEquals(10L, response.categoryId());
        assertEquals("Food", response.categoryName());
        assertEquals(new BigDecimal("10000.00"), response.amount());
        assertEquals(BigDecimal.ZERO, response.spent());
        assertEquals(new BigDecimal("10000.00"), response.remaining());

        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldFail_WhenBudgetAlreadyExists() {

        Category category = mock(Category.class);

        when(user.getId()).thenReturn(1L);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(user);
        when(category.getId()).thenReturn(10L);
        when(category.isActive()).thenReturn(true);
        when(category.getType()).thenReturn("EXPENSE");

        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                1L, 10L, 11, 2026
        )).thenReturn(true);

        CreateBudgetRequest request = new CreateBudgetRequest(
                10L,
                new BigDecimal("10000"),
                11,
                2026
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.createBudget(request)
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldFail_WhenCategoryDoesNotExist() {

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.empty());

        CreateBudgetRequest request = new CreateBudgetRequest(
                10L,
                new BigDecimal("10000"),
                11,
                2026
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> budgetService.createBudget(request)
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldFail_WhenCategoryBelongsToAnotherUser() {

        Category category = mock(Category.class);

        when(user.getId()).thenReturn(1L);

        User anotherUser = mock(User.class);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(anotherUser);
        when(anotherUser.getId()).thenReturn(2L);

        CreateBudgetRequest request = new CreateBudgetRequest(
                10L,
                new BigDecimal("10000"),
                11,
                2026
        );

        assertThrows(
                AccessDeniedException.class,
                () -> budgetService.createBudget(request)
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldFail_WhenCategoryIsInactive() {

        Category category = mock(Category.class);

        when(user.getId()).thenReturn(1L);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(user);
        when(category.isActive()).thenReturn(false);

        CreateBudgetRequest request = new CreateBudgetRequest(
                10L,
                new BigDecimal("10000"),
                11,
                2026
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.createBudget(request)
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldFail_WhenCategoryIsIncomeType() {

        Category category = mock(Category.class);

        when(user.getId()).thenReturn(1L);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(user);
        when(category.isActive()).thenReturn(true);
        when(category.getType()).thenReturn("INCOME");

        CreateBudgetRequest request = new CreateBudgetRequest(
                10L,
                new BigDecimal("10000"),
                11,
                2026
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.createBudget(request)
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_ShouldFail_WhenYearExceedsAllowedLimit() {

        int invalidYear = LocalDate.now().getYear() + 11;

        CreateBudgetRequest request = new CreateBudgetRequest(
                10L,
                new BigDecimal("10000"),
                11,
                invalidYear
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.createBudget(request)
        );

        verifyNoInteractions(categoryRepository);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // ==========================================
    // BUDGET RETRIEVAL TESTS
    // ==========================================

    @Test
    void getBudgets_ShouldReturnBudgetsForSelectedMonth() {

        when(user.getId()).thenReturn(1L);

        Category category = mock(Category.class);
        Budget budget = mock(Budget.class);
        Transaction transaction = mock(Transaction.class);

        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 11, 2026))
                .thenReturn(List.of(budget));

        when(budget.getYear()).thenReturn(2026);
        when(budget.getMonth()).thenReturn(11);
        when(budget.getUser()).thenReturn(user);
        when(budget.getCategory()).thenReturn(category);
        when(budget.getAmount()).thenReturn(new BigDecimal("10000"));

        when(category.getId()).thenReturn(10L);
        when(category.getName()).thenReturn("Food");

        when(transaction.getType()).thenReturn("EXPENSE");
        when(transaction.getCategory()).thenReturn(category);
        when(transaction.getAmount()).thenReturn(new BigDecimal("2500"));

        when(transactionRepository.findByUserIdAndTransactionDateBetween(
                eq(1L),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of(transaction));

        var response = budgetService.getBudgets(11, 2026);

        assertEquals(1, response.size());
        assertEquals("Food", response.get(0).categoryName());
        assertEquals(new BigDecimal("10000"), response.get(0).amount());
        assertEquals(new BigDecimal("2500"), response.get(0).spent());
        assertEquals(new BigDecimal("7500"), response.get(0).remaining());

        verify(budgetRepository)
                .findByUserIdAndMonthAndYear(1L, 11, 2026);
    }

    @Test
    void getBudgets_ShouldReturnEmptyList_WhenNoBudgetsExist() {

        when(user.getId()).thenReturn(1L);

        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 11, 2026))
                .thenReturn(List.of());

        var response = budgetService.getBudgets(11, 2026);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(transactionRepository, never())
                .findByUserIdAndTransactionDateBetween(
                        anyLong(),
                        any(LocalDate.class),
                        any(LocalDate.class)
                );
    }

    @Test
    void getBudgets_ShouldPreserveNegativeRemaining_WhenOverspending() {

        when(user.getId()).thenReturn(1L);

        Category category = mock(Category.class);
        Budget budget = mock(Budget.class);
        Transaction transaction = mock(Transaction.class);

        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 11, 2026))
                .thenReturn(List.of(budget));

        when(budget.getYear()).thenReturn(2026);
        when(budget.getMonth()).thenReturn(11);
        when(budget.getUser()).thenReturn(user);
        when(budget.getCategory()).thenReturn(category);
        when(budget.getAmount()).thenReturn(new BigDecimal("10000"));

        when(category.getId()).thenReturn(10L);
        when(category.getName()).thenReturn("Food");

        when(transaction.getType()).thenReturn("EXPENSE");
        when(transaction.getCategory()).thenReturn(category);
        when(transaction.getAmount()).thenReturn(new BigDecimal("12000"));

        when(transactionRepository.findByUserIdAndTransactionDateBetween(
                eq(1L),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of(transaction));

        var response = budgetService.getBudgets(11, 2026);

        assertEquals(new BigDecimal("12000"), response.get(0).spent());
        assertEquals(new BigDecimal("-2000"), response.get(0).remaining());
    }

    @Test
    void getBudgets_ShouldFail_WhenMonthIsInvalid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.getBudgets(13, 2026)
        );

        verifyNoInteractions(budgetRepository);
        verifyNoInteractions(transactionRepository);
    }

    // ==========================================
    // BUDGET UPDATE TESTS
    // ==========================================

    @Test
    void updateBudget_ShouldSucceed_WhenAmountIsValid() {

        when(user.getId()).thenReturn(1L);

        Budget budget = mock(Budget.class);
        Category category = mock(Category.class);

        when(budgetRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(budget));

        when(budget.getYear()).thenReturn(2026);
        when(budget.getMonth()).thenReturn(11);
        when(budget.getUser()).thenReturn(user);
        when(budget.getCategory()).thenReturn(category);
        when(budget.getAmount()).thenReturn(new BigDecimal("15000"));

        when(category.getId()).thenReturn(20L);
        when(category.getName()).thenReturn("Food");

        when(budgetRepository.save(budget)).thenReturn(budget);

        when(transactionRepository.findByUserIdAndTransactionDateBetween(
                eq(1L),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(List.of());

        var response = budgetService.updateBudget(
                10L,
                new BigDecimal("15000")
        );

        assertEquals(new BigDecimal("15000"), response.amount());
        assertEquals(BigDecimal.ZERO, response.spent());
        assertEquals(new BigDecimal("15000"), response.remaining());

        verify(budget).setAmount(new BigDecimal("15000"));
        verify(budgetRepository).save(budget);
    }

    @Test
    void updateBudget_ShouldFail_WhenAmountIsZero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.updateBudget(
                        10L,
                        BigDecimal.ZERO
                )
        );

        verifyNoInteractions(budgetRepository);
    }

    @Test
    void updateBudget_ShouldFail_WhenAmountIsNegative() {

        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.updateBudget(
                        10L,
                        new BigDecimal("-500")
                )
        );

        verifyNoInteractions(budgetRepository);
    }

    @Test
    void updateBudget_ShouldFail_WhenAmountIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.updateBudget(10L, null)
        );

        verifyNoInteractions(budgetRepository);
    }

    @Test
    void updateBudget_ShouldFail_WhenBudgetDoesNotExistOrBelongsToAnotherUser() {

        when(user.getId()).thenReturn(1L);

        when(budgetRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> budgetService.updateBudget(
                        10L,
                        new BigDecimal("15000")
                )
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }


    // ==========================================
    // BUDGET DELETION TESTS
    // ==========================================

    @Test
    void deleteBudget_ShouldSucceed_WhenBudgetBelongsToUser() {

        when(user.getId()).thenReturn(1L);

        Budget budget = mock(Budget.class);

        when(budgetRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(budget));

        budgetService.deleteBudget(10L);

        verify(budgetRepository).delete(budget);
    }

    @Test
    void deleteBudget_ShouldFail_WhenBudgetDoesNotExistOrBelongsToAnotherUser() {

        when(user.getId()).thenReturn(1L);

        when(budgetRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> budgetService.deleteBudget(10L)
        );

        verify(budgetRepository, never()).delete(any(Budget.class));
    }

}
