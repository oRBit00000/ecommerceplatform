package hr.algebra.ecommerceplatform.listener;

import hr.algebra.ecommerceplatform.service.LoginAuditService;
import hr.algebra.ecommerceplatform.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;

@Component
public class AuthenticationSuccessListener {

    private final LoginAuditService loginAuditService;
    private final ClientIpResolver clientIpResolver;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    public AuthenticationSuccessListener(LoginAuditService loginAuditService,
                                         ClientIpResolver clientIpResolver,
                                         ObjectProvider<HttpServletRequest> requestProvider) {
        this.loginAuditService = loginAuditService;
        this.clientIpResolver = clientIpResolver;
        this.requestProvider = requestProvider;
    }

    // Records a login audit entry whenever authentication succeeds during a web request.
    @EventListener
    @Transactional
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request == null) {
            return;
        }

        String username = event.getAuthentication().getName();
        String ipAddress = clientIpResolver.resolve(request);
        loginAuditService.logSuccessfulLogin(username, ipAddress);
    }
}
