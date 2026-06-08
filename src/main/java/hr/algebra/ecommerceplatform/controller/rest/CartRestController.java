package hr.algebra.ecommerceplatform.controller.rest;

import hr.algebra.ecommerceplatform.dto.CartSummaryDTO;
import hr.algebra.ecommerceplatform.service.CartService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/cart")
public class CartRestController {

    private static final String MESSAGE_KEY = "message";

    private final CartService cartService;

    public CartRestController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/summary")
    public ResponseEntity<CartSummaryDTO> summary() {
        return ResponseEntity.ok(cartService.getCartSummary());
    }

    @PostMapping("/add")
    public ResponseEntity<Object> add(@RequestParam Long productId, @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            cartService.addProduct(productId, quantity);
            return ResponseEntity.ok(cartService.getCartSummary());
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, ex.getMessage()));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Object> update(@RequestParam Long productId, @RequestParam Integer quantity) {
        try {
            cartService.updateQuantity(productId, quantity);
            return ResponseEntity.ok(cartService.getCartSummary());
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(MESSAGE_KEY, ex.getMessage()));
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<CartSummaryDTO> remove(@RequestParam Long productId) {
        cartService.removeProduct(productId);
        return ResponseEntity.ok(cartService.getCartSummary());
    }

    @PostMapping("/clear")
    public ResponseEntity<CartSummaryDTO> clear() {
        cartService.clearCart();
        return ResponseEntity.ok(cartService.getCartSummary());
    }
}
