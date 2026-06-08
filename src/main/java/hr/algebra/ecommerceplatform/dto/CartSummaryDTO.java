package hr.algebra.ecommerceplatform.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryDTO {
    private List<CartItemDTO> items;
    private Integer totalItems;
    private BigDecimal totalPrice;
}
