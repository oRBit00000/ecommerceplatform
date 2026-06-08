package hr.algebra.ecommerceplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Category name cannot be empty.")
    @Size(min = 3, max = 80)
    private String name;

    @Size(max = 255)
    private String description;
}
