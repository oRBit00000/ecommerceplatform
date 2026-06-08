package hr.algebra.ecommerceplatform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name cannot be empty.")
    @Size(min = 3, max = 120)
    private String name;

    @NotNull(message = "Category must be selected.")
    private Long categoryId;

    private String categoryName;

    @Size(max = 500, message = "Description can contain up to 500 characters.")
    private String description;

    @NotNull(message = "Price cannot be null.")
    @Positive(message = "Price must be greater than zero.")
    private BigDecimal price;

    @NotNull(message = "Stock quantity cannot be null.")
    @Min(value = 0, message = "Stock quantity cannot be negative.")
    private Integer stockQuantity;
}
