package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.dto.LoginAuditDTO;
import java.util.List;

public interface LoginAuditService {
    void logSuccessfulLogin(String username, String ipAddress);
    List<LoginAuditDTO> findAll();
}
