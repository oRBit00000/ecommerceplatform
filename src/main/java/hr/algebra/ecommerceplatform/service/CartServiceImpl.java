package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.CartItemDTO;
import hr.algebra.ecommerceplatform.dto.CartSummaryDTO;
import hr.algebra.ecommerceplatform.model.Product;
import hr.algebra.ecommerceplatform.repository.ProductRepository;
import hr.algebra.ecommerceplatform.util.Constants;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    private final ObjectProvider<HttpSession> sessionProvider;
    private final ProductRepository productRepository;

    public CartServiceImpl(ObjectProvider<HttpSession> sessionProvider, ProductRepository productRepository) {
        this.sessionProvider = sessionProvider;
        this.productRepository = productRepository;
    }

    @Override
    public CartSummaryDTO getCartSummary() {
        Map<Long, Integer> cart = getCart();
        List<CartItemDTO> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalItems = 0;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product == null) {
                continue;
            }
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            items.add(new CartItemDTO(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    entry.getValue(),
                    lineTotal,
                    product.getStockQuantity()
            ));
            totalPrice = totalPrice.add(lineTotal);
            totalItems += entry.getValue();
        }

        return new CartSummaryDTO(items, totalItems, totalPrice);
    }

    @Override
    public void addProduct(Long productId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            quantity = 1;
        }
        Map<Long, Integer> cart = getCart();
        int newQuantity = cart.getOrDefault(productId, 0) + quantity;
        validateStock(productId, newQuantity);
        cart.put(productId, newQuantity);
        getSession().setAttribute(Constants.CART_SESSION_KEY, cart);
    }

    @Override
    public void updateQuantity(Long productId, Integer quantity) {
        Map<Long, Integer> cart = getCart();
        if (quantity == null || quantity <= 0) {
            cart.remove(productId);
        } else {
            validateStock(productId, quantity);
            cart.put(productId, quantity);
        }
        getSession().setAttribute(Constants.CART_SESSION_KEY, cart);
    }

    @Override
    public void removeProduct(Long productId) {
        Map<Long, Integer> cart = getCart();
        cart.remove(productId);
        getSession().setAttribute(Constants.CART_SESSION_KEY, cart);
    }

    @Override
    public void clearCart() {
        getSession().setAttribute(Constants.CART_SESSION_KEY, new LinkedHashMap<Long, Integer>());
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart() {
        Object cartObject = getSession().getAttribute(Constants.CART_SESSION_KEY);
        if (cartObject instanceof Map<?, ?> cart) {
            return (Map<Long, Integer>) cart;
        }
        Map<Long, Integer> cart = new LinkedHashMap<>();
        getSession().setAttribute(Constants.CART_SESSION_KEY, cart);
        return cart;
    }

    private HttpSession getSession() {
        return sessionProvider.getObject();
    }

    private void validateStock(Long productId, Integer requestedQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        if (requestedQuantity > product.getStockQuantity()) {
            throw new IllegalStateException("Only " + product.getStockQuantity() + " item(s) of " + product.getName() + " are currently in stock.");
        }
    }
}
