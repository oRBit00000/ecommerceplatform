package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.CartSummaryDTO;

public interface CartService {
    CartSummaryDTO getCartSummary();
    void addProduct(Long productId, Integer quantity);
    void updateQuantity(Long productId, Integer quantity);
    void removeProduct(Long productId);
    void clearCart();
}
