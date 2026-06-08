package hr.algebra.ecommerceplatform.configuration;

import hr.algebra.ecommerceplatform.model.RefreshToken;
import hr.algebra.ecommerceplatform.service.JwtService;
import hr.algebra.ecommerceplatform.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class MvcAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    public static final String SESSION_ACCESS_TOKEN_KEY = "sessionAccessToken";

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public MvcAuthenticationSuccessHandler(JwtService jwtService,
                                           RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        String accessToken = jwtService.generateToken(username);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(username);

        request.getSession().setAttribute(SESSION_ACCESS_TOKEN_KEY, accessToken);
        request.getSession().setAttribute("sessionRefreshTokenId", refreshToken.getId());

        response.sendRedirect("/mvc/products/search");
    }
}
