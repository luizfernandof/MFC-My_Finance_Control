package br.com.devl.mfc.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Relatórios PDF")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class ReportController {

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	private User getAuthenticatedUser() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	@GetMapping(value = "/transactions/monthly", produces = MediaType.APPLICATION_PDF_VALUE)
	@Operation(summary = "Gerar relatório mensal de transações em PDF")
	public ResponseEntity<byte[]> generateMonthlyTransactionsPdf(
				@Parameter(description = "Mês (1-12)", example = "4") @RequestParam @Min(1) @Max(12) int month,
				@Parameter(description = "Ano", example = "2026") @RequestParam @Min(2000) @Max(2200) int year) {
		User user = getAuthenticatedUser();
		byte[] pdfBytes = reportService.generateMonthlyTransactionsPdf(user, month, year);
		return ResponseEntity.ok().header("Content-Disposition",
				"attachment; filename=transacoes_" + month + "_" + year + ".pdf").body(pdfBytes);
	}

	@GetMapping(value = "/categories/monthly", produces = MediaType.APPLICATION_PDF_VALUE)
	@Operation(summary = "Gerar análise mensal de gastos por categoria em PDF")
	public ResponseEntity<byte[]> generateMonthlyCategoryAnalysisPdf(
			@Parameter(description = "Mês (1-12)", example = "4") @RequestParam @Min(1) @Max(12) int month,
			@Parameter(description = "Ano", example = "2026") @RequestParam @Min(2000) @Max(2200) int year) {
		User user = getAuthenticatedUser();
		byte[] pdfBytes = reportService.generateMonthlyCategoryAnalysisPdf(user, month, year);
		return ResponseEntity.ok().header("Content-Disposition",
				"attachment; filename=gastos_por_categoria_" + month + "_" + year + ".pdf").body(pdfBytes);
	}
}
