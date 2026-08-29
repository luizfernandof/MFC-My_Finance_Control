package br.com.devl.mfc.dto;

import br.com.devl.mfc.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO (
			@NotBlank(message = "O nome é obrigatório")
			@Size(max = 80, message = "O nome deve ter no máximo 80 caracteres")
			String name,
			@NotNull(message = "O tipo é obrigatório")
			CategoryType type
) {}
