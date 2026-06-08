package hr.algebra.ecommerceplatform.controller.mvc;

import hr.algebra.ecommerceplatform.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mvc/cart")
public class CartMvcController {

    private static final String REDIRECT_CART = "redirect:/mvc/cart";

    private final CartService cartService;

    public CartMvcController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String cart(Model model) {
        model.addAttribute("cartSummary", cartService.getCartSummary());
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") Integer quantity,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addProduct(productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return REDIRECT_CART;
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam Long productId, @RequestParam Integer quantity,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(productId, quantity);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return REDIRECT_CART;
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId) {
        cartService.removeProduct(productId);
        return REDIRECT_CART;
    }

    @PostMapping("/clear")
    public String clearCart() {
        cartService.clearCart();
        return REDIRECT_CART;
    }
}
