package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.ProductDTO;
import hr.algebra.ecommerceplatform.form.ProductSearchForm;
import hr.algebra.ecommerceplatform.model.Category;
import hr.algebra.ecommerceplatform.model.Product;
import hr.algebra.ecommerceplatform.repository.CategoryRepository;
import hr.algebra.ecommerceplatform.repository.ProductRepository;
import hr.algebra.ecommerceplatform.specification.ProductSpecification;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Returns all products as DTOs.
    @Override
    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // Finds one product by id.
    @Override
    public Optional<ProductDTO> findById(Long id) {
        return productRepository.findById(id).map(this::toDTO);
    }

    // Finds products whose names match the search text.
    @Override
    public List<ProductDTO> findByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDTO)
                .toList();
    }

    // Finds products belonging to the given category name.
    @Override
    public List<ProductDTO> findByCategory(String category) {
        return productRepository.findByCategoryNameIgnoreCase(category).stream()
                .map(this::toDTO)
                .toList();
    }

    // Saves a new product.
    @Override
    public ProductDTO save(ProductDTO product) {
        return toDTO(productRepository.save(toEntity(product)));
    }

    // Updates an existing product.
    @Override
    public ProductDTO update(Long id, ProductDTO product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        Category category = getCategory(product.getCategoryId());

        existing.setName(product.getName());
        existing.setCategory(category);
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStockQuantity(product.getStockQuantity());

        return toDTO(productRepository.save(existing));
    }

    // Deletes a product by id.
    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    // Searches products using the dynamic filter form values.
    @Override
    public List<ProductDTO> findBySearchCriteria(ProductSearchForm productSearchForm) {
        return productRepository.findAll(
                        ProductSpecification.containsName(productSearchForm.getName())
                                .and(ProductSpecification.containsCategory(productSearchForm.getCategory()))
                                .and(ProductSpecification.priceGreaterThanOrEqual(productSearchForm.getPriceFrom()))
                                .and(ProductSpecification.priceLessThanOrEqual(productSearchForm.getPriceTo()))
                ).stream()
                .map(this::toDTO)
                .toList();
    }

    // Returns the distinct category names used by current products.
    @Override
    public List<String> findProductCategories() {
        return productRepository.findDistinctCategories();
    }

    // Converts a product entity into a DTO.
    private ProductDTO toDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }

    // Converts a product DTO into a new entity.
    private Product toEntity(ProductDTO productDTO) {
        Category category = getCategory(productDTO.getCategoryId());
        return new Product(
                productDTO.getName(),
                category,
                productDTO.getDescription(),
                productDTO.getPrice(),
                productDTO.getStockQuantity()
        );
    }

    // Loads the category referenced by the product DTO.
    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
    }
}
