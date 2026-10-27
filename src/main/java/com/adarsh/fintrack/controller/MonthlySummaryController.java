
package com.adarsh.fintrack.controller;

import com.adarsh.fintrack.dto.MonthlySummaryResponse;
import com.adarsh.fintrack.service.MonthlySummaryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class MonthlySummaryController {

    private final MonthlySummaryService monthlySummaryService;

    public MonthlySummaryController(
            MonthlySummaryService monthlySummaryService) {
        this.monthlySummaryService = monthlySummaryService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam String month) {

        return ResponseEntity.ok(
                monthlySummaryService.getMonthlySummary(month));
    }
}
