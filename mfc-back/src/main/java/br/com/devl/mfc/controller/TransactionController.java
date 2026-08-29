package br.com.devl.mfc.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.dto.TransactionRequestDTO;
import br.com.devl.mfc.dto.TransactionResponseDTO;
import br.com.devl.mfc.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/transactions")
@Validated
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	private User getAuthenticatedUser() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	@PostMapping
	public ResponseEntity<TransactionResponseDTO> create(@Valid @RequestBody TransactionRequestDTO dto) {
		User user = getAuthenticatedUser();
		return ResponseEntity.ok(transactionService.create(dto, user));
	}

	@GetMapping
	public ResponseEntity<Page<TransactionResponseDTO>> list(@RequestParam @Min(1) @Max(12) int month,
			@RequestParam @Min(2000) @Max(2200) int year,
			@RequestParam(defaultValue = "") @Size(max = 100) String search,
			@PageableDefault(size = 10, sort = "date") Pageable pageable) {
		User user = getAuthenticatedUser();
		return ResponseEntity.ok(transactionService.list(user, month, year, search, pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TransactionResponseDTO> findById(@PathVariable Long id) {
		User user = getAuthenticatedUser();
		return ResponseEntity.ok(transactionService.findById(id, user));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TransactionResponseDTO> update(@PathVariable Long id,
				@Valid @RequestBody TransactionRequestDTO dto) {
		User user = getAuthenticatedUser();
		return ResponseEntity.ok(transactionService.update(id, dto, user));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		User user = getAuthenticatedUser();
		transactionService.delete(id, user);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}/group-forward")
	public ResponseEntity<Void> deleteGroupForward(@PathVariable Long id) {
		User user = getAuthenticatedUser();
		transactionService.deleteGroupForward(id, user);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}/group")
	public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
		User user = getAuthenticatedUser();
		transactionService.deleteGroup(id, user);
		return ResponseEntity.noContent().build();
	}

}
