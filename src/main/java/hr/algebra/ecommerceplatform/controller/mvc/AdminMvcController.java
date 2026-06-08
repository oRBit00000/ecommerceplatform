package hr.algebra.ecommerceplatform.controller.mvc;

import hr.algebra.ecommerceplatform.form.OrderSearchForm;
import hr.algebra.ecommerceplatform.service.CategoryService;
import hr.algebra.ecommerceplatform.service.LoginAuditService;
import hr.algebra.ecommerceplatform.service.OrderService;
import hr.algebra.ecommerceplatform.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mvc/admin")
public class AdminMvcController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final LoginAuditService loginAuditService;
    private final OrderService orderService;

    public AdminMvcController(ProductService productService,
                              CategoryService categoryService,
                              LoginAuditService loginAuditService,
                              OrderService orderService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.loginAuditService = loginAuditService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("audits", loginAuditService.findAll());
        model.addAttribute("orders", orderService.searchAllOrders(new OrderSearchForm()));
        return "adminDashboard";
    }
}
