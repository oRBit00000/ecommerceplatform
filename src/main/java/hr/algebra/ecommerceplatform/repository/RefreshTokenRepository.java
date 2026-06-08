package hr.algebra.ecommerceplatform.repository;

import hr.algebra.ecommerceplatform.model.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser_Name(String name);
    void deleteByToken(String token);
    void deleteByUser_Name(String name);
}
