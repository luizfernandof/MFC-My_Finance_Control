package br.com.devl.mfc.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.dto.CategoryRequestDTO;
import br.com.devl.mfc.dto.CategoryResponseDTO;
import br.com.devl.mfc.entity.Category;
import br.com.devl.mfc.exception.BusinessException;
import br.com.devl.mfc.repository.CategoryRepository;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Transactional
	public CategoryResponseDTO create(CategoryRequestDTO dto, User user) {
		String name = dto.name().trim();

		if (categoryRepository.existsByNameIgnoreCaseAndUser(name, user)) {
			throw new BusinessException("CATEGORY_ALREADY_EXISTS", "A categoria " + name + " já existe!",
					HttpStatus.CONFLICT);
		}

		Category category = new Category();
		category.setName(name);
		category.setType(dto.type());
		category.setUser(user);

		Category saved = categoryRepository.save(category);

		return toResponseDTO(saved);
	}

	@Transactional(readOnly = true)
	public Page<CategoryResponseDTO> list(User user, String search, Pageable pageable) {
		String normalizedSearch = search == null ? "" : search.trim();
		Page<Category> categories = normalizedSearch.isEmpty()
				? categoryRepository.findByUser(user, pageable)
				: categoryRepository.findByUserAndNameContainingIgnoreCase(user, normalizedSearch, pageable);
		return categories.map(this::toResponseDTO);
	}

	@Transactional(readOnly = true)
	public List<CategoryResponseDTO> listOptions(User user) {
		return categoryRepository.findByUserOrderByNameAsc(user).stream().map(this::toResponseDTO).toList();
	}

	public CategoryResponseDTO findById(Long id, User user) {

		Category category = categoryRepository.findByIdAndUser(id, user)
					.orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Categoria não encontrada",
							HttpStatus.NOT_FOUND));

		return toResponseDTO(category);

	}

	@Transactional
	public CategoryResponseDTO update(Long id, CategoryRequestDTO dto, User user) {

		Category category = categoryRepository.findByIdAndUser(id, user)
					.orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Categoria não encontrada",
							HttpStatus.NOT_FOUND));
		String name = dto.name().trim();
		if (categoryRepository.existsByNameIgnoreCaseAndUserAndIdNot(name, user, id)) {
			throw new BusinessException("CATEGORY_ALREADY_EXISTS", "A categoria " + name + " já existe!",
					HttpStatus.CONFLICT);
		}

		category.setName(name);
		category.setType(dto.type());

		Category updated = categoryRepository.save(category);

		return toResponseDTO(updated);

	}

	@Transactional
	public void delete(Long id, User user) {
		Category category = categoryRepository.findByIdAndUser(id, user)
					.orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Categoria não encontrada",
							HttpStatus.NOT_FOUND));
		categoryRepository.delete(category);
	}

	private CategoryResponseDTO toResponseDTO(Category category) {
		return new CategoryResponseDTO(category.getId(), category.getName(), category.getType());
	}
}
