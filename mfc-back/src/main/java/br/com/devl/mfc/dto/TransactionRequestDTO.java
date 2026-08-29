package br.com.devl.mfc.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.devl.mfc.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransactionRequestDTO(
		    @NotBlank(message = "A descrição é obrigatória")
		    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
		    String description,
		    @NotNull(message = "O valor é obrigatório")
		    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
		    @Digits(integer = 13, fraction = 2, message = "O valor deve ter no máximo 13 inteiros e 2 decimais")
		    BigDecimal amount,
		    @NotNull(message = "A data é obrigatória")
		    LocalDate date,
		    @NotNull(message = "A categoria é obrigatória")
		    Long categoryId,
		    @NotNull(message = "O tipo é obrigatório")
		    TransactionType type,
		    @Min(value = 1, message = "A quantidade de parcelas deve ser pelo menos 1")
		    @Max(value = 120, message = "A quantidade de parcelas deve ser no máximo 120")
		    Integer installments,
		    boolean recurring,
		    @Min(value = 2, message = "A recorrência deve ter pelo menos 2 ocorrências")
		    @Max(value = 120, message = "A recorrência deve ter no máximo 120 ocorrências")
		    Integer occurrences
		) {}
