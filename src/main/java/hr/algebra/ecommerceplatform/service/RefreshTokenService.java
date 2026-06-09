package hr.algebra.ecommerceplatform.service;

import hr.algebra.ecommerceplatform.model.RefreshToken;
import hr.algebra.ecommerceplatform.model.User;
import hr.algebra.ecommerceplatform.repository.RefreshTokenRepository;
import hr.algebra.ecommerceplatform.repository.UserRepository;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-token-expiration:7d}")
    private Duration refreshTokenExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken createRefreshToken(String username) {
        refreshTokenRepository.findByUser_Name(username).ifPresent(existingToken -> {
            refreshTokenRepository.delete(existingToken);
            refreshTokenRepository.flush();
        });

        User user = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenExpiration));
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalStateException("Refresh token expired. Please log in again.");
        }
        return refreshToken;
    }

    public void deleteByToken(String token) {
        if (refreshTokenRepository.findByToken(token).isEmpty()) {
            throw new IllegalStateException("Refresh token not found.");
        }
        refreshTokenRepository.deleteByToken(token);
    }

    public void deleteIfPresent(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUser_Name(username);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByUsername(String username) {
        return refreshTokenRepository.findByUser_Name(username);
    }
}
