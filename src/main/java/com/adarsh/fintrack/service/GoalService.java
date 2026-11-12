package com.adarsh.fintrack.service;

import com.adarsh.fintrack.dto.CreateGoalRequest;
import com.adarsh.fintrack.dto.GoalContributionRequest;
import com.adarsh.fintrack.dto.GoalContributionResponse;
import com.adarsh.fintrack.dto.GoalResponse;
import com.adarsh.fintrack.entity.Goal;
import com.adarsh.fintrack.entity.GoalContribution;
import com.adarsh.fintrack.entity.User;
import com.adarsh.fintrack.exception.ResourceNotFoundException;
import com.adarsh.fintrack.repository.GoalContributionRepository;
import com.adarsh.fintrack.repository.GoalRepository;
import com.adarsh.fintrack.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;
    private final UserRepository userRepository;

    public GoalService(
            GoalRepository goalRepository,
            GoalContributionRepository contributionRepository,
            UserRepository userRepository) {

        this.goalRepository = goalRepository;
        this.contributionRepository = contributionRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        ));
    }

    private Goal getOwnedGoal(Long goalId, User user) {

        return goalRepository.findByIdAndUserId(
                        goalId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Goal not found: " + goalId
                        ));
    }

    private GoalResponse toResponse(Goal goal) {

        BigDecimal savedAmount =
                contributionRepository.getTotalContributed(goal.getId());

        if (savedAmount == null) {
            savedAmount = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount =
                goal.getTargetAmount().subtract(savedAmount);

        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        double progress = savedAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        goal.getTargetAmount(),
                        2,
                        RoundingMode.HALF_UP
                )
                .doubleValue();

        progress = Math.min(progress, 100.0);

        return new GoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getDescription(),
                goal.getTargetAmount(),
                savedAmount,
                remainingAmount,
                progress,
                goal.getDeadline(),
                goal.getStatus()
        );
    }

    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request) {

        User user = getAuthenticatedUser();

        if (goalRepository.existsByUserIdAndNameIgnoreCase(
                user.getId(),
                request.name().trim())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A goal with this name already exists"
            );
        }

        Goal goal = new Goal();

        goal.setUser(user);
        goal.setName(request.name().trim());
        goal.setDescription(request.description());
        goal.setTargetAmount(request.targetAmount());
        goal.setDeadline(request.deadline());
        goal.setStatus("ACTIVE");

        return toResponse(goalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getMyGoals() {

        User user = getAuthenticatedUser();

        return goalRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GoalResponse addContribution(
            Long goalId,
            GoalContributionRequest request) {

        User user = getAuthenticatedUser();

        // Lock the user's goal row to prevent concurrent over-contributions.
        Goal goal = goalRepository
                .findByIdAndUserIdForUpdate(goalId, user.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Goal not found: " + goalId
                        ));

        // A completed goal must never accept further contributions.
        if ("COMPLETED".equalsIgnoreCase(goal.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot add contributions to a completed goal."
            );
        }

        BigDecimal currentSaved =
                contributionRepository.getTotalContributed(goalId);

        if (currentSaved == null) {
            currentSaved = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount =
                goal.getTargetAmount().subtract(currentSaved);

        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        // Prevent contributions from exceeding the goal's target.
        if (request.amount().compareTo(remainingAmount) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Contribution exceeds the remaining goal amount. "
                            + "Remaining amount: " + remainingAmount
            );
        }

        GoalContribution contribution = new GoalContribution();

        contribution.setGoal(goal);
        contribution.setAmount(request.amount());

        contribution.setContributionDate(
                request.contributionDate() != null
                        ? request.contributionDate()
                        : LocalDate.now()
        );

        contribution.setNote(request.note());

        contributionRepository.save(contribution);

        BigDecimal totalSaved =
                contributionRepository.getTotalContributed(goalId);

        if (totalSaved == null) {
            totalSaved = BigDecimal.ZERO;
        }

        if (totalSaved.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("COMPLETED");
        } else {
            goal.setStatus("ACTIVE");
        }

        goalRepository.save(goal);

        return toResponse(goal);
    }

    @Transactional(readOnly = true)
    public List<GoalContributionResponse> getContributions(Long goalId) {

        User user = getAuthenticatedUser();

        // Verify ownership before retrieving contribution history.
        getOwnedGoal(goalId, user);

        return contributionRepository
                .findByGoalIdOrderByContributionDateDesc(goalId)
                .stream()
                .map(contribution -> new GoalContributionResponse(
                        contribution.getId(),
                        contribution.getAmount(),
                        contribution.getContributionDate(),
                        contribution.getNote(),
                        contribution.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void deleteGoal(Long goalId) {

        User user = getAuthenticatedUser();

        Goal goal = getOwnedGoal(goalId, user);

        List<GoalContribution> contributions =
                contributionRepository
                        .findByGoalIdOrderByContributionDateDesc(goalId);

        contributionRepository.deleteAll(contributions);

        goalRepository.delete(goal);
    }
}