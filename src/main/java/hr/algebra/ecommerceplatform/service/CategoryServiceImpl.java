package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.CategoryDTO;
import hr.algebra.ecommerceplatform.model.Category;
import hr.algebra.ecommerceplatform.repository.CategoryRepository;
import hr.algebra.ecommerceplatform.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public Optional<CategoryDTO> findById(Long id) {
        return categoryRepository.findById(id).map(this::toDTO);
    }

    @Override
    public CategoryDTO save(CategoryDTO categoryDTO) {
        return toDTO(categoryRepository.save(new Category(categoryDTO.getName(), categoryDTO.getDescription())));
    }

    @Override
    public CategoryDTO update(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        return toDTO(categoryRepository.save(category));
    }

    @Override
    public void deleteById(Long id) {
        if (productRepository.countByCategoryId(id) > 0) {
            throw new IllegalStateException("Cannot delete a category that still has products.");
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDTO toDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getDescription());
    }
}
