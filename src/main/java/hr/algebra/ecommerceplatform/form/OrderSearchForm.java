package hr.algebra.ecommerceplatform.form;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchForm {
    private String customerUsername;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
