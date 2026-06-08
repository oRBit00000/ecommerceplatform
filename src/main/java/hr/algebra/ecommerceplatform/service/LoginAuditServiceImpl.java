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

    @Override
    public void logSuccessfulLogin(String username, String ipAddress) {
        loginAuditRepository.save(new LoginAudit(null, username, ipAddress, LocalDateTime.now()));
    }

    @Override
    public List<LoginAuditDTO> findAll() {
        return loginAuditRepository.findAllByOrderByLoggedInAtDesc().stream()
                .map(audit -> new LoginAuditDTO(audit.getUsername(), audit.getIpAddress(), audit.getLoggedInAt()))
                .toList();
    }
}
