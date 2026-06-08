package hr.algebra.ecommerceplatform.repository;

import hr.algebra.ecommerceplatform.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("select distinct p.category.name from Product p order by p.category.name")
    List<String> findDistinctCategories();

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryNameIgnoreCase(String categoryName);

    long countByCategoryId(Long categoryId);
}
