package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.configuration.MvcAuthenticationSuccessHandler;
import hr.algebra.ecommerceplatform.dto.AuthRequestDTO;
import hr.algebra.ecommerceplatform.dto.JwtResponseDTO;
import hr.algebra.ecommerceplatform.dto.RefreshTokenRequestDTO;
import hr.algebra.ecommerceplatform.model.RefreshToken;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    // Handles REST login by checking username/password and issuing new tokens.
    public JwtResponseDTO authenticate(AuthRequestDTO authRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
        );
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authentication.getName());
        return JwtResponseDTO.builder()
                .accessToken(jwtService.generateToken(authentication.getName()))
                .refreshToken(refreshToken.getToken())
                .build();
    }

    // Handles both API-token refresh and MVC session-token refresh in one place.
    public JwtResponseDTO refreshToken(Authentication authentication,
                                       HttpSession session,
                                       RefreshTokenRequestDTO refreshTokenRequestDTO) {
        if (refreshTokenRequestDTO != null
                && refreshTokenRequestDTO.getRefreshToken() != null
                && !refreshTokenRequestDTO.getRefreshToken().isBlank()) {
            // API refresh flow: client sends a refresh token in the request body.
            RefreshToken refreshToken;
            try {
                refreshToken = refreshTokenService.findByToken(refreshTokenRequestDTO.getRefreshToken())
                        .map(refreshTokenService::verifyExpiration)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found."));
            } catch (IllegalStateException ex) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex);
            }

            return JwtResponseDTO.builder()
                    .accessToken(jwtService.generateToken(refreshToken.getUser().getName()))
                    .refreshToken(refreshToken.getToken())
                    .build();
        }

        // Browser session flow: no body token means we refresh the session-managed access token.
        if (authentication == null) {
            return JwtResponseDTO.builder().build();
        }

        String currentAccessToken = (String) session.getAttribute(MvcAuthenticationSuccessHandler.SESSION_ACCESS_TOKEN_KEY);
        if (currentAccessToken != null && jwtService.isTokenValid(currentAccessToken)) {
            // If the current session token is still valid, reuse it instead of issuing a new one.
            return JwtResponseDTO.builder()
                    .accessToken(currentAccessToken)
                    .build();
        }

        RefreshToken refreshToken;
        try {
            refreshToken = refreshTokenService.findByUsername(authentication.getName())
                    .map(refreshTokenService::verifyExpiration)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found."));
        } catch (IllegalStateException ex) {
            terminateAuthenticatedSession(authentication, session);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                terminateAuthenticatedSession(authentication, session);
            }
            throw ex;
        }

        String newAccessToken = jwtService.generateToken(refreshToken.getUser().getName());
        session.setAttribute(MvcAuthenticationSuccessHandler.SESSION_ACCESS_TOKEN_KEY, newAccessToken);

        return JwtResponseDTO.builder()
                .accessToken(newAccessToken)
                .build();
    }

    // Handles REST logout by deleting the provided refresh token.
    public ResponseEntity<Map<String, String>> logout(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        String refreshToken = refreshTokenRequestDTO != null ? refreshTokenRequestDTO.getRefreshToken() : null;
        try {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new IllegalStateException("Refresh token is required.");
            }
            refreshTokenService.deleteByToken(refreshToken);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    // Cleans up the logged-in browser session when refresh can no longer succeed.
    private void terminateAuthenticatedSession(Authentication authentication, HttpSession session) {
        if (authentication != null) {
            refreshTokenService.deleteByUsername(authentication.getName());
        }

        SecurityContextHolder.clearContext();
        session.invalidate();
    }
}
