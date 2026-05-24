package br.com.devl.mfc.dto;

import java.math.BigDecimal;

public record MonthlyTrendDTO(
		int month,
		int year,
		BigDecimal totalIncome,
		BigDecimal totalExpense
) {}