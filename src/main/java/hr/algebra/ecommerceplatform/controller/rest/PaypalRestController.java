package hr.algebra.ecommerceplatform.controller.rest;

import hr.algebra.ecommerceplatform.service.OrderService;
import hr.algebra.ecommerceplatform.service.PaypalService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/paypal")
public class PaypalRestController {

    private static final String MESSAGE_KEY = "message";

    private final PaypalService paypalService;
    private final OrderService orderService;

    public PaypalRestController(PaypalService paypalService, OrderService orderService) {
        this.paypalService = paypalService;
        this.orderService = orderService;
    }

    @PostMapping("/orders/create")
    public ResponseEntity<Object> createOrder(HttpServletRequest request) {
        try {
            String appBaseUrl = request.getScheme() + "://" + request.getServerName()
                    + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());
            return ResponseEntity.ok(paypalService.createOrder(appBaseUrl));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, ex.getMessage()));
        }
    }

    @PostMapping("/orders/{orderId}/capture")
    public ResponseEntity<Object> captureOrder(@PathVariable String orderId, Authentication authentication) {
        try {
            paypalService.captureOrder(orderId);
            return ResponseEntity.ok(orderService.checkoutWithPaypal(authentication.getName(), orderId));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, ex.getMessage()));
        }
    }
}
