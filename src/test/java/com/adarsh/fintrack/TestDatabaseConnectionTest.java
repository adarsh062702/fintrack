
package com.adarsh.fintrack;

import com.adarsh.fintrack.entity.User;
import com.adarsh.fintrack.repository.UserRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class TestDatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldConnectToDedicatedTestDatabase() throws Exception {

        try (Connection connection = dataSource.getConnection()) {

            assertNotNull(connection);

            try (var statement = connection.createStatement();
                 var result = statement.executeQuery("""
                         SELECT TABLE_NAME, COLUMN_NAME, COLUMN_DEFAULT, EXTRA
                         FROM INFORMATION_SCHEMA.COLUMNS
                         WHERE TABLE_SCHEMA = 'fintrack_test'
                           AND TABLE_NAME IN ('users', 'user_settings', 'categories', 'budgets')
                           AND COLUMN_NAME IN ('created_at', 'updated_at')
                         ORDER BY TABLE_NAME, COLUMN_NAME
                         """)) {

                while (result.next()) {
                    System.out.printf(
                            "%s | %s | %s | %s%n",
                            result.getString("TABLE_NAME"),
                            result.getString("COLUMN_NAME"),
                            result.getString("COLUMN_DEFAULT"),
                            result.getString("EXTRA")
                    );
                }
            }

            try (var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT DATABASE()")) {

                assertTrue(result.next());
                assertEquals("fintrack_test", result.getString(1));
            }
        }
    }

    @Test
    @Transactional
    void shouldAutomaticallyManageUserTimestamps() throws InterruptedException {

        // Arrange: Create a new user
        User user = new User();

        user.setName("Timestamp Test");
        user.setEmail("timestamp-test@example.com");
        user.setPasswordHash("dummy-password-hash");
        user.setRole("USER");
        user.setActive(true);

        // Act: Persist the user
        User savedUser = userRepository.saveAndFlush(user);

        LocalDateTime originalCreatedAt = savedUser.getCreatedAt();
        LocalDateTime originalUpdatedAt = savedUser.getUpdatedAt();

        // Assert: Both timestamps should be generated
        assertNotNull(
                originalCreatedAt,
                "createdAt should be generated automatically"
        );

        assertNotNull(
                originalUpdatedAt,
                "updatedAt should be generated automatically"
        );

        // Allow enough time to distinguish timestamp values
        Thread.sleep(1100);

        // Act: Update the existing user
        savedUser.setName("Updated Timestamp Test");

        userRepository.saveAndFlush(savedUser);

        // Clear persistence context to verify persisted values
        entityManager.clear();

        User updatedUser = userRepository.findById(savedUser.getId())
                .orElseThrow();

        // Assert: createdAt must remain unchanged
        assertEquals(
                originalCreatedAt,
                updatedUser.getCreatedAt(),
                "createdAt must not change after updating a user"
        );

        // Assert: updatedAt must change
        assertNotNull(updatedUser.getUpdatedAt());

        assertNotEquals(
                originalUpdatedAt,
                updatedUser.getUpdatedAt(),
                "updatedAt should change automatically after an update"
        );

        assertTrue(
                updatedUser.getUpdatedAt().isAfter(originalUpdatedAt),
                "updatedAt should be later than its original value"
        );
    }
}
