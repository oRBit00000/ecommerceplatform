package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.CartSummaryDTO;
import hr.algebra.ecommerceplatform.dto.OrderDTO;
import hr.algebra.ecommerceplatform.dto.OrderItemDTO;
import hr.algebra.ecommerceplatform.form.CheckoutForm;
import hr.algebra.ecommerceplatform.form.OrderSearchForm;
import hr.algebra.ecommerceplatform.model.OrderItem;
import hr.algebra.ecommerceplatform.model.PaymentMethod;
import hr.algebra.ecommerceplatform.model.Product;
import hr.algebra.ecommerceplatform.model.PurchaseOrder;
import hr.algebra.ecommerceplatform.model.User;
import hr.algebra.ecommerceplatform.repository.OrderRepository;
import hr.algebra.ecommerceplatform.repository.ProductRepository;
import hr.algebra.ecommerceplatform.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository,
                            CartService cartService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Override
    @Transactional
    public OrderDTO checkout(String username, CheckoutForm checkoutForm) {
        return createOrder(username, checkoutForm.getPaymentMethod());
    }

    @Override
    @Transactional
    public OrderDTO checkoutWithPaypal(String username, String paypalOrderId) {
        return createOrder(username, PaymentMethod.PAYPAL);
    }

    private OrderDTO createOrder(String username, PaymentMethod paymentMethod) {
        CartSummaryDTO cartSummary = cartService.getCartSummary();
        if (cartSummary.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        User user = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        PurchaseOrder order = new PurchaseOrder();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);
        order.setTotalAmount(cartSummary.getTotalPrice());

        List<OrderItem> items = cartSummary.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));

                    if (item.getQuantity() > product.getStockQuantity()) {
                        throw new IllegalStateException("Only " + product.getStockQuantity() + " item(s) of " + product.getName() + " are currently in stock.");
                    }

                    product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setUnitPrice(item.getUnitPrice());
                    return orderItem;
                }).toList();

        order.setItems(items);
        PurchaseOrder savedOrder = orderRepository.save(order);
        cartService.clearCart();
        return toDTO(savedOrder);
    }

    @Override
    public List<OrderDTO> findOrdersForCustomer(String username) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getUser().getName().equalsIgnoreCase(username))
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<OrderDTO> searchAllOrders(OrderSearchForm orderSearchForm) {
        LocalDateTime from = orderSearchForm.getDateFrom() == null
                ? LocalDateTime.of(2000, 1, 1, 0, 0)
                : orderSearchForm.getDateFrom().atStartOfDay();
        LocalDateTime to = orderSearchForm.getDateTo() == null
                ? LocalDateTime.now().plusDays(1)
                : orderSearchForm.getDateTo().plusDays(1).atStartOfDay();

        return orderRepository.findAll().stream()
                .filter(order -> !order.getCreatedAt().isBefore(from) && order.getCreatedAt().isBefore(to))
                .filter(order -> orderSearchForm.getCustomerUsername() == null
                        || orderSearchForm.getCustomerUsername().isBlank()
                        || order.getUser().getName().toLowerCase().contains(orderSearchForm.getCustomerUsername().toLowerCase()))
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .map(this::toDTO)
                .toList();
    }

    private OrderDTO toDTO(PurchaseOrder order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
        return new OrderDTO(
                order.getId(),
                order.getUser().getName(),
                order.getCreatedAt(),
                order.getPaymentMethod(),
                order.getTotalAmount(),
                itemDTOs
        );
    }
}
