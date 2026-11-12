
package com.adarsh.fintrack.repository;

import com.adarsh.fintrack.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserId(Long userId);
    List<Category> findByUser_IdOrUserIsNull(Long userId);
}
