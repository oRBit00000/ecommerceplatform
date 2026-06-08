package hr.algebra.ecommerceplatform.form;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchForm {
    private String name;
    private String category;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
}
