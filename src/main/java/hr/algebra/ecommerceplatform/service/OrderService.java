package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.OrderDTO;
import hr.algebra.ecommerceplatform.form.CheckoutForm;
import hr.algebra.ecommerceplatform.form.OrderSearchForm;
import java.util.List;

public interface OrderService {
    OrderDTO checkout(String username, CheckoutForm checkoutForm);
    OrderDTO checkoutWithPaypal(String username, String paypalOrderId);
    List<OrderDTO> findOrdersForCustomer(String username);
    List<OrderDTO> searchAllOrders(OrderSearchForm orderSearchForm);
}
