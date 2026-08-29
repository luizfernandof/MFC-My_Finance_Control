package br.com.devl.mfc.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.validation.annotation.Validated;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.dto.CategoryRequestDTO;
import br.com.devl.mfc.dto.CategoryResponseDTO;
import br.com.devl.mfc.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/categories")
@Validated
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	private User getAuthenticatedUser() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	@PostMapping
	public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO dto) {
		User user = getAuthenticatedUser();
		return ResponseEntity.ok(categoryService.create(dto, user));
	}
	
	@GetMapping
	public ResponseEntity<Page<CategoryResponseDTO>> list(
			@RequestParam(defaultValue = "") @Size(max = 100) String search,
			@PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
		User user = getAuthenticatedUser();
		return ResponseEntity.ok(categoryService.list(user, search, pageable));
	}

	@GetMapping("/options")
	public ResponseEntity<List<CategoryResponseDTO>> listOptions() {
		User user = getAuthenticatedUser();
		return ResponseEntity.ok(categoryService.listOptions(user));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<CategoryResponseDTO> findById(@PathVariable Long id) {
		User user = getAuthenticatedUser();
		return ResponseEntity.ok(categoryService.findById(id, user));
	} 
	
	@PutMapping("/{id}")
	public ResponseEntity<CategoryResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO dto) {
		User user = getAuthenticatedUser();
		return ResponseEntity.ok(categoryService.update(id, dto, user));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		User user = getAuthenticatedUser();
		categoryService.delete(id, user);
		return ResponseEntity.noContent().build();
	}
	
}
