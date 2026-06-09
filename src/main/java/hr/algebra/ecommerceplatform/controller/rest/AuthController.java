package hr.algebra.ecommerceplatform.controller.rest;

import hr.algebra.ecommerceplatform.dto.AuthRequestDTO;
import hr.algebra.ecommerceplatform.dto.JwtResponseDTO;
import hr.algebra.ecommerceplatform.dto.RefreshTokenRequestDTO;
import hr.algebra.ecommerceplatform.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/rest/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // REST login: validates credentials and returns a fresh access/refresh token pair.
    @PostMapping("/login")
    public JwtResponseDTO authenticate(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        return authService.authenticate(authRequestDTO);
    }

    // Refreshes tokens for API clients or refreshes the session token for the MVC browser flow.
    @PostMapping("/refresh")
    public JwtResponseDTO refreshToken(Authentication authentication,
                                       HttpSession session,
                                       @RequestBody(required = false) RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return authService.refreshToken(authentication, session, refreshTokenRequestDTO);
    }

    // REST logout: deletes the provided refresh token and returns a simple success message.
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody(required = false) RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return authService.logout(refreshTokenRequestDTO);
    }

}
