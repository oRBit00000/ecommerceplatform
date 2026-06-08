package hr.algebra.ecommerceplatform.dto;

import hr.algebra.ecommerceplatform.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String customerUsername;
    private LocalDateTime createdAt;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
}
