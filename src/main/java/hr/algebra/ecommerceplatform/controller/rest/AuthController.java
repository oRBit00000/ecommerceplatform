package hr.algebra.ecommerceplatform.controller.rest;

import hr.algebra.ecommerceplatform.dto.AuthRequestDTO;
import hr.algebra.ecommerceplatform.dto.JwtResponseDTO;
import hr.algebra.ecommerceplatform.dto.RefreshTokenRequestDTO;
import hr.algebra.ecommerceplatform.model.RefreshToken;
import hr.algebra.ecommerceplatform.configuration.MvcAuthenticationSuccessHandler;
import hr.algebra.ecommerceplatform.service.JwtService;
import hr.algebra.ecommerceplatform.service.RefreshTokenService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/rest/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public JwtResponseDTO authenticate(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
        );
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authentication.getName());
        return JwtResponseDTO.builder()
                .accessToken(jwtService.generateToken(authentication.getName()))
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @PostMapping("/refresh")
    public JwtResponseDTO refreshToken(Authentication authentication,
                                       HttpSession session,
                                       @RequestBody(required = false) RefreshTokenRequestDTO refreshTokenRequestDTO) {
        if (refreshTokenRequestDTO != null
                && refreshTokenRequestDTO.getRefreshToken() != null
                && !refreshTokenRequestDTO.getRefreshToken().isBlank()) {
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequestDTO.getRefreshToken())
                    .map(refreshTokenService::verifyExpiration)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found."));

            return JwtResponseDTO.builder()
                    .accessToken(jwtService.generateToken(refreshToken.getUser().getName()))
                    .refreshToken(refreshToken.getToken())
                    .build();
        }

        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated.");
        }

        String currentAccessToken = (String) session.getAttribute(MvcAuthenticationSuccessHandler.SESSION_ACCESS_TOKEN_KEY);
        if (currentAccessToken != null && jwtService.isTokenValid(currentAccessToken)) {
            return JwtResponseDTO.builder()
                    .accessToken(currentAccessToken)
                    .build();
        }

        RefreshToken refreshToken = refreshTokenService.findByUsername(authentication.getName())
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found."));

        String newAccessToken = jwtService.generateToken(refreshToken.getUser().getName());
        session.setAttribute(MvcAuthenticationSuccessHandler.SESSION_ACCESS_TOKEN_KEY, newAccessToken);

        return JwtResponseDTO.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @PostMapping("/logout")
    public void logout(@RequestBody(required = false) RefreshTokenRequestDTO refreshTokenRequestDTO) {
        String refreshToken = refreshTokenRequestDTO != null ? refreshTokenRequestDTO.getRefreshToken() : null;
        try {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new IllegalStateException("Refresh token is required.");
            }
            refreshTokenService.deleteByToken(refreshToken);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

}
