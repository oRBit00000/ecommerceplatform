package hr.algebra.ecommerceplatform.controller.mvc;

import hr.algebra.ecommerceplatform.dto.RegistrationDTO;
import hr.algebra.ecommerceplatform.service.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class AuthMvcController {

    private static final String REGISTER_VIEW = "register";

    private final UserRegistrationService userRegistrationService;

    public AuthMvcController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationDTO", new RegistrationDTO());
        return REGISTER_VIEW;
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegistrationDTO registrationDTO,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            return REGISTER_VIEW;
        }

        try {
            userRegistrationService.registerCustomer(registrationDTO);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return REGISTER_VIEW;
        }

        return "redirect:/login?registered";
    }
}
