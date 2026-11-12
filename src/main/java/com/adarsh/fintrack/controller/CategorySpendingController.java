package com.adarsh.fintrack.controller;

import com.adarsh.fintrack.dto.CategorySpendingResponse;
import com.adarsh.fintrack.service.CategorySpendingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class CategorySpendingController {

    private final CategorySpendingService categorySpendingService;

    public CategorySpendingController(
            CategorySpendingService categorySpendingService) {
        this.categorySpendingService = categorySpendingService;
    }

    @GetMapping("/category-spending")
    public ResponseEntity<List<CategorySpendingResponse>> getCategorySpending(
            @RequestParam String month) {

        return ResponseEntity.ok(
                categorySpendingService.getCategorySpending(month));
    }
}