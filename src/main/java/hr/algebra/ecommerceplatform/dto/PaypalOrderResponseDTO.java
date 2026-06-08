package hr.algebra.ecommerceplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaypalOrderResponseDTO {
    private String orderId;
    private String approvalUrl;
}
