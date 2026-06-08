package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.ProductDTO;
import hr.algebra.ecommerceplatform.form.ProductSearchForm;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<ProductDTO> findAll();
    Optional<ProductDTO> findById(Long id);
    List<ProductDTO> findByName(String name);
    List<ProductDTO> findByCategory(String category);
    ProductDTO save(ProductDTO product);
    ProductDTO update(Long id, ProductDTO product);
    void deleteById(Long id);
    List<ProductDTO> findBySearchCriteria(ProductSearchForm productSearchForm);
    List<String> findProductCategories();
}
