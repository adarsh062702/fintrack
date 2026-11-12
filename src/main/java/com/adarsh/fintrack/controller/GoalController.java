
package com.adarsh.fintrack.controller;

import com.adarsh.fintrack.dto.CreateGoalRequest;
import com.adarsh.fintrack.dto.GoalContributionRequest;
import com.adarsh.fintrack.dto.GoalContributionResponse;
import com.adarsh.fintrack.dto.GoalResponse;
import com.adarsh.fintrack.service.GoalService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getMyGoals() {
        return ResponseEntity.ok(goalService.getMyGoals());
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody CreateGoalRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.createGoal(request));
    }

    @PostMapping("/{goalId}/contributions")
    public ResponseEntity<GoalResponse> addContribution(
            @PathVariable Long goalId,
            @Valid @RequestBody GoalContributionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(goalService.addContribution(goalId, request));
    }

    @GetMapping("/{goalId}/contributions")
    public ResponseEntity<List<GoalContributionResponse>> getContributions(
            @PathVariable Long goalId) {

        return ResponseEntity.ok(
                goalService.getContributions(goalId));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long goalId) {

        goalService.deleteGoal(goalId);

        return ResponseEntity.noContent().build();
    }
}
