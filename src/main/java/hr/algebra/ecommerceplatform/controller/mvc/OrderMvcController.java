package hr.algebra.ecommerceplatform.controller.mvc;

import hr.algebra.ecommerceplatform.form.CheckoutForm;
import hr.algebra.ecommerceplatform.form.OrderSearchForm;
import hr.algebra.ecommerceplatform.model.PaymentMethod;
import hr.algebra.ecommerceplatform.service.CartService;
import hr.algebra.ecommerceplatform.service.OrderService;
import hr.algebra.ecommerceplatform.service.PaypalService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mvc/orders")
public class OrderMvcController {

    private static final String CHECKOUT_VIEW = "checkout";
    private static final String ORDERS_ATTRIBUTE = "orders";
    private static final String PAYPAL_SUCCESS_ATTRIBUTE = "paypalSuccess";
    private static final String PAYPAL_REDIRECT_URL_ATTRIBUTE = "paypalRedirectUrl";
    private static final String PAYPAL_MESSAGE_ATTRIBUTE = "paypalMessage";

    private final OrderService orderService;
    private final CartService cartService;
    private final PaypalService paypalService;

    public OrderMvcController(OrderService orderService, CartService cartService, PaypalService paypalService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.paypalService = paypalService;
    }

    @GetMapping("/checkout")
    public String checkout(Authentication authentication, Model model) {
        populateCheckoutModel(authentication, model, new CheckoutForm());
        return CHECKOUT_VIEW;
    }

    @PostMapping("/checkout")
    public String processCheckout(@Valid @ModelAttribute CheckoutForm checkoutForm,
                                  BindingResult result,
                                  Authentication authentication,
                                  Model model) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            populateCheckoutModel(authentication, model, checkoutForm);
            return CHECKOUT_VIEW;
        }

        if (checkoutForm.getPaymentMethod() == PaymentMethod.PAYPAL) {
            populateCheckoutModel(authentication, model, checkoutForm);
            model.addAttribute("checkoutError", "Choose the PayPal option and complete payment with the PayPal button below.");
            return CHECKOUT_VIEW;
        }

        try {
            model.addAttribute("order", orderService.checkout(authentication.getName(), checkoutForm));
            return "orderSuccess";
        } catch (IllegalStateException ex) {
            populateCheckoutModel(authentication, model, checkoutForm);
            model.addAttribute("checkoutError", ex.getMessage());
            return CHECKOUT_VIEW;
        }
    }

    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        model.addAttribute(ORDERS_ATTRIBUTE, orderService.findOrdersForCustomer(authentication.getName()));
        return "orderHistory";
    }

    @GetMapping("/paypal/complete")
    public String paypalComplete(@RequestParam("token") String orderId,
                                 Authentication authentication,
                                 Model model) {
        String redirectUrl = "/mvc/orders/history";
        try {
            if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                throw new IllegalStateException("Please log in again before completing PayPal checkout.");
            }

            paypalService.captureOrder(orderId);
            orderService.checkoutWithPaypal(authentication.getName(), orderId);
            model.addAttribute(PAYPAL_SUCCESS_ATTRIBUTE, true);
            model.addAttribute(PAYPAL_REDIRECT_URL_ATTRIBUTE, redirectUrl);
            model.addAttribute(PAYPAL_MESSAGE_ATTRIBUTE, "PayPal payment approved successfully.");
        } catch (IllegalStateException ex) {
            model.addAttribute(PAYPAL_SUCCESS_ATTRIBUTE, false);
            model.addAttribute(PAYPAL_REDIRECT_URL_ATTRIBUTE, redirectUrl);
            model.addAttribute(PAYPAL_MESSAGE_ATTRIBUTE, ex.getMessage());
        }
        return "paypalPopupResult";
    }

    @GetMapping("/paypal/cancel")
    public String paypalCancel(Model model) {
        model.addAttribute(PAYPAL_SUCCESS_ATTRIBUTE, false);
        model.addAttribute(PAYPAL_REDIRECT_URL_ATTRIBUTE, "/mvc/orders/checkout");
        model.addAttribute(PAYPAL_MESSAGE_ATTRIBUTE, "PayPal checkout was cancelled.");
        return "paypalPopupResult";
    }

    @GetMapping("/admin")
    public String adminOrders(Model model) {
        model.addAttribute("orderSearchForm", new OrderSearchForm());
        model.addAttribute(ORDERS_ATTRIBUTE, orderService.searchAllOrders(new OrderSearchForm()));
        return "adminOrders";
    }

    @PostMapping("/admin")
    public String filterAdminOrders(@ModelAttribute OrderSearchForm orderSearchForm, Model model) {
        model.addAttribute("orderSearchForm", orderSearchForm);
        model.addAttribute(ORDERS_ATTRIBUTE, orderService.searchAllOrders(orderSearchForm));
        return "adminOrders";
    }

    private void populateCheckoutModel(Authentication authentication, Model model, CheckoutForm checkoutForm) {
        model.addAttribute("cartSummary", cartService.getCartSummary());
        model.addAttribute("checkoutForm", checkoutForm);
        model.addAttribute("guestCheckout", authentication == null || authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("paypalConfigured", paypalService.isConfigured());
    }
}
