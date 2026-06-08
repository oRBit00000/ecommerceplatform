package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.CategoryDTO;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryDTO> findAll();
    Optional<CategoryDTO> findById(Long id);
    CategoryDTO save(CategoryDTO categoryDTO);
    CategoryDTO update(Long id, CategoryDTO categoryDTO);
    void deleteById(Long id);
}
