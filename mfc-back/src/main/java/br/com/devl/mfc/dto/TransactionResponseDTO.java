package br.com.devl.mfc.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.devl.mfc.enums.TransactionType;
import br.com.devl.mfc.enums.TransactionGroupType;

public record TransactionResponseDTO(
		Long id,
		String description,
		BigDecimal amount,
		LocalDate date,
			TransactionType type,
			String categoryName,
			String groupId,
			TransactionGroupType groupType
) {}
