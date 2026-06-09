package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.LoginAuditDTO;
import hr.algebra.ecommerceplatform.model.LoginAudit;
import hr.algebra.ecommerceplatform.repository.LoginAuditRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LoginAuditServiceImpl implements LoginAuditService {

    private final LoginAuditRepository loginAuditRepository;

    public LoginAuditServiceImpl(LoginAuditRepository loginAuditRepository) {
        this.loginAuditRepository = loginAuditRepository;
    }

    // Saves one successful login event with its timestamp and IP address.
    @Override
    public void logSuccessfulLogin(String username, String ipAddress) {
        loginAuditRepository.save(new LoginAudit(null, username, ipAddress, LocalDateTime.now()));
    }

    // Returns all login audit entries ordered from newest to oldest.
    @Override
    public List<LoginAuditDTO> findAll() {
        return loginAuditRepository.findAllByOrderByLoggedInAtDesc().stream()
                .map(audit -> new LoginAuditDTO(audit.getUsername(), audit.getIpAddress(), audit.getLoggedInAt()))
                .toList();
    }
}
