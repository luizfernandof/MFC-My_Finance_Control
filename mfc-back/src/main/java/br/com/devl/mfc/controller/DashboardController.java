package br.com.devl.mfc.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.devl.mfc.dto.DashboardSummaryDTO;
import br.com.devl.mfc.dto.MonthlyTrendDTO;
import br.com.devl.mfc.service.DashboardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/dashboard")
@Validated
public class DashboardController {

	private final DashboardService service;

	public DashboardController(DashboardService service) {
		this.service = service;
	}

	@GetMapping("/summary")
	public ResponseEntity<DashboardSummaryDTO> getSummary(@RequestParam @Min(1) @Max(12) int month,
			@RequestParam @Min(2000) @Max(2200) int year) {
		return ResponseEntity.ok(service.getSummary(month, year));
	}

	@GetMapping("/monthly-trend")
	public ResponseEntity<List<MonthlyTrendDTO>> getMonthlyTrend(@RequestParam @Min(1) @Max(12) int month,
			@RequestParam @Min(2000) @Max(2200) int year) {
		return ResponseEntity.ok(service.getMonthlyTrend(month, year));
	}
}
