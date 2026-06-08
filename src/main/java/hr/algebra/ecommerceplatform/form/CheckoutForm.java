package hr.algebra.ecommerceplatform.form;

import hr.algebra.ecommerceplatform.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutForm {
    @NotNull(message = "Please choose a payment method.")
    private PaymentMethod paymentMethod;
}
