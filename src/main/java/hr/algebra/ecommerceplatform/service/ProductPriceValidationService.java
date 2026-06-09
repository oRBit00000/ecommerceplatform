package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.ProductDTO;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProductPriceValidationService {

    private static final BigDecimal MAX_ALLOWED_PRICE = new BigDecimal("100000.00");

    // Validates that the entered product price stays within the allowed limit.
    public Optional<String> validateProductPrice(ProductDTO productDTO) {
        if (productDTO.getPrice() != null && productDTO.getPrice().compareTo(MAX_ALLOWED_PRICE) > 0) {
            return Optional.of("Price must not be higher than " + MAX_ALLOWED_PRICE + ".");
        }
        return Optional.empty();
    }
}
