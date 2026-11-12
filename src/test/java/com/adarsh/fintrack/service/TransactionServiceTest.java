
package com.adarsh.fintrack.service;

import com.adarsh.fintrack.dto.CreateTransactionRequest;
import com.adarsh.fintrack.dto.TransactionResponse;
import com.adarsh.fintrack.entity.Category;
import com.adarsh.fintrack.entity.Transaction;
import com.adarsh.fintrack.entity.User;
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

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category category;

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
        when(user.getId()).thenReturn(1L);

        category = mock(Category.class);
        when(category.getUser()).thenReturn(user);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CreateTransactionRequest createRequest(String type) {

        CreateTransactionRequest request =
                new CreateTransactionRequest();

        request.setCategoryId(10L);
        request.setType(type);
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Test transaction");
        request.setTransactionDate(LocalDate.of(2026, 11, 8));
        request.setPaymentMethod("CASH");

        return request;
    }

    @Test
    void createTransaction_ShouldSucceed_WhenRequestIsValid() {

        when(category.getId()).thenReturn(10L);
        when(category.getName()).thenReturn("Food");
        when(category.getType()).thenReturn("EXPENSE");
        when(category.isActive()).thenReturn(true);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction =
                            invocation.getArgument(0);

                    transaction.setId(100L);

                    return transaction;
                });

        TransactionResponse response =
                transactionService.createTransaction(
                        createRequest("EXPENSE")
                );

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Food", response.categoryName());
        assertEquals("EXPENSE", response.type());
        assertEquals(
                new BigDecimal("100.00"),
                response.amount()
        );

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_ShouldFail_WhenTypeDoesNotMatchCategory() {

        when(category.getType()).thenReturn("EXPENSE");
        when(category.isActive()).thenReturn(true);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transactionService.createTransaction(
                                createRequest("INCOME")
                        )
                );

        assertEquals(
                "Transaction type must match the category type: EXPENSE",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void createTransaction_ShouldFail_WhenCategoryIsInactive() {

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.isActive()).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        createRequest("EXPENSE")
                )
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }
}
