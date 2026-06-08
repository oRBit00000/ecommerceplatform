package hr.algebra.ecommerceplatform.specification;

import hr.algebra.ecommerceplatform.model.Product;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> containsName(String name) {
        return (root, query, builder) -> {
            if (name == null || name.isBlank()) {
                return builder.conjunction();
            }
            return builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Product> containsCategory(String category) {
        return (root, query, builder) -> {
            if (category == null || category.isBlank()) {
                return builder.conjunction();
            }
            return builder.equal(builder.lower(root.get("category").get("name")), category.toLowerCase());
        };
    }

    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal priceFrom) {
        return (root, query, builder) ->
                priceFrom == null ? builder.conjunction() : builder.greaterThanOrEqualTo(root.get("price"), priceFrom);
    }

    public static Specification<Product> priceLessThanOrEqual(BigDecimal priceTo) {
        return (root, query, builder) ->
                priceTo == null ? builder.conjunction() : builder.lessThanOrEqualTo(root.get("price"), priceTo);
    }
}
