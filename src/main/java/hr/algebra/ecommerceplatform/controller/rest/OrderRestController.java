package hr.algebra.ecommerceplatform.controller.rest;

import hr.algebra.ecommerceplatform.dto.LoginAuditDTO;
import hr.algebra.ecommerceplatform.dto.OrderDTO;
import hr.algebra.ecommerceplatform.form.OrderSearchForm;
import hr.algebra.ecommerceplatform.service.LoginAuditService;
import hr.algebra.ecommerceplatform.service.OrderService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/orders")
public class OrderRestController {

    private final OrderService orderService;
    private final LoginAuditService loginAuditService;

    public OrderRestController(OrderService orderService, LoginAuditService loginAuditService) {
        this.orderService = orderService;
        this.loginAuditService = loginAuditService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderDTO>> myOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.findOrdersForCustomer(authentication.getName()));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<OrderDTO>> adminOrders(@RequestParam(required = false) String customer,
                                                      @RequestParam(required = false) String from,
                                                      @RequestParam(required = false) String to) {
        OrderSearchForm form = new OrderSearchForm();
        form.setCustomerUsername(customer);
        if (from != null && !from.isBlank()) {
            form.setDateFrom(java.time.LocalDate.parse(from));
        }
        if (to != null && !to.isBlank()) {
            form.setDateTo(java.time.LocalDate.parse(to));
        }
        return ResponseEntity.ok(orderService.searchAllOrders(form));
    }

    @GetMapping("/admin/logins")
    public ResponseEntity<List<LoginAuditDTO>> loginHistory() {
        return ResponseEntity.ok(loginAuditService.findAll());
    }
}
