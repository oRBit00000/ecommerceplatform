package hr.algebra.ecommerceplatform.repository;

import hr.algebra.ecommerceplatform.model.LoginAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
    List<LoginAudit> findAllByOrderByLoggedInAtDesc();
}
