
package com.adarsh.fintrack.service;

import com.adarsh.fintrack.dto.CategoryRequest;
import com.adarsh.fintrack.dto.CategoryResponse;
import com.adarsh.fintrack.entity.Category;
import com.adarsh.fintrack.entity.User;
import com.adarsh.fintrack.exception.ResourceNotFoundException;
import com.adarsh.fintrack.repository.CategoryRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

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
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
    }

    private Category createMockCategory(
            Long id,
            String name,
            String type,
            boolean active) {

        Category category = mock(Category.class);

        when(category.getId()).thenReturn(id);
        when(category.getName()).thenReturn(name);
        when(category.getType()).thenReturn(type);
        when(category.isActive()).thenReturn(active);

        return category;
    }

    @Test
    void getAllCategories_ShouldReturnGlobalAndUserCategories() {
        authenticate();
        when(user.getId()).thenReturn(1L);

        Category globalCategory = createMockCategory(
                1L, "Food", "EXPENSE", true
        );

        Category personalCategory = createMockCategory(
                10L, "Salary", "INCOME", true
        );

        when(categoryRepository.findByUser_IdOrUserIsNull(1L))
                .thenReturn(List.of(globalCategory, personalCategory));

        List<CategoryResponse> response =
                categoryService.getAllCategories();

        assertEquals(2, response.size());
        assertEquals("Food", response.get(0).name());
        assertEquals("Salary", response.get(1).name());
        assertEquals("EXPENSE", response.get(0).type());
        assertEquals("INCOME", response.get(1).type());

        verify(categoryRepository)
                .findByUser_IdOrUserIsNull(1L);
    }

    @Test
    void createCategory_ShouldSucceed_WhenRequestIsValid() {
        authenticate();

        CategoryRequest request =
                new CategoryRequest("Food", "EXPENSE");

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category category = invocation.getArgument(0);
                    category.setId(10L);
                    return category;
                });

        CategoryResponse response =
                categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Food", response.name());
        assertEquals("EXPENSE", response.type());
        assertTrue(response.active());

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldTrimCategoryName() {
        authenticate();

        CategoryRequest request =
                new CategoryRequest("  Food  ", " expense ");

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category category = invocation.getArgument(0);
                    category.setId(11L);
                    return category;
                });

        CategoryResponse response =
                categoryService.createCategory(request);

        assertEquals("Food", response.name());
        assertEquals("EXPENSE", response.type());
    }

    @Test
    void createCategory_ShouldFail_WhenRequestIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createCategory(null)
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void createCategory_ShouldFail_WhenNameIsBlank() {
        authenticate();

        CategoryRequest request =
                new CategoryRequest("   ", "EXPENSE");

        assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createCategory(request)
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void createCategory_ShouldFail_WhenNameExceeds100Characters() {
        authenticate();

        String longName = "A".repeat(101);

        CategoryRequest request =
                new CategoryRequest(longName, "EXPENSE");

        assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createCategory(request)
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void createCategory_ShouldFail_WhenTypeIsInvalid() {
        authenticate();

        CategoryRequest request =
                new CategoryRequest("Food", "SAVINGS");

        assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createCategory(request)
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldSucceed_WhenUserOwnsCategory() {
        authenticate();

        when(user.getId()).thenReturn(1L);

        Category category = mock(Category.class);
        User owner = mock(User.class);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(1L);

        when(categoryRepository.save(category))
                .thenReturn(category);

        when(category.getId()).thenReturn(10L);
        when(category.getName()).thenReturn("Transport");
        when(category.getType()).thenReturn("EXPENSE");
        when(category.isActive()).thenReturn(true);

        CategoryResponse response =
                categoryService.updateCategory(
                        10L,
                        new CategoryRequest("Transport", "EXPENSE")
                );

        assertEquals("Transport", response.name());
        assertEquals("EXPENSE", response.type());

        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_ShouldFail_WhenCategoryDoesNotExist() {
        authenticate();

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(
                        99L,
                        new CategoryRequest("Food", "EXPENSE")
                )
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldFail_WhenCategoryBelongsToAnotherUser() {
        authenticate();

        when(user.getId()).thenReturn(1L);

        Category category = mock(Category.class);
        User owner = mock(User.class);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(2L);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.updateCategory(
                        10L,
                        new CategoryRequest("Food", "EXPENSE")
                )
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldFail_WhenCategoryIsGlobal() {
        authenticate();

        Category category = mock(Category.class);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(null);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.updateCategory(
                        10L,
                        new CategoryRequest("Food Updated", "EXPENSE")
                )
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldSucceed_WhenUserOwnsCategory() {
        authenticate();

        when(user.getId()).thenReturn(1L);

        Category category = mock(Category.class);
        User owner = mock(User.class);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(1L);

        categoryService.deleteCategory(10L);

        verify(categoryRepository).delete(category);
        verify(categoryRepository, never()).existsById(anyLong());
    }

    @Test
    void deleteCategory_ShouldFail_WhenCategoryDoesNotExist() {
        authenticate();

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(99L)
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldFail_WhenCategoryBelongsToAnotherUser() {
        authenticate();

        when(user.getId()).thenReturn(1L);

        Category category = mock(Category.class);
        User owner = mock(User.class);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(2L);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.deleteCategory(10L)
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldFail_WhenCategoryIsGlobal() {
        authenticate();

        Category category = mock(Category.class);

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        when(category.getUser()).thenReturn(null);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.deleteCategory(10L)
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }

    @Test
    void getAllCategories_ShouldFail_WhenUserIsUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.getAllCategories()
        );

        verifyNoInteractions(userRepository, categoryRepository);
    }
}
