package hr.algebra.ecommerceplatform.configuration;

import hr.algebra.ecommerceplatform.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class MvcLogoutHandler implements LogoutHandler {

    private final RefreshTokenService refreshTokenService;

    public MvcLogoutHandler(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            refreshTokenService.deleteByUsername(authentication.getName());
        }

        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(MvcAuthenticationSuccessHandler.SESSION_ACCESS_TOKEN_KEY);
            request.getSession(false).removeAttribute("sessionRefreshTokenId");
        }
    }
}
