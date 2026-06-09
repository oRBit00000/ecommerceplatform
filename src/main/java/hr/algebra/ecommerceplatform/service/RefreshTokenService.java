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

    // Creates a new refresh token and replaces any older token for the same user.
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

    // Finds a refresh token by its raw token value.
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Checks whether a refresh token is still valid and removes it if it expired.
    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalStateException("Refresh token expired. Please log in again.");
        }
        return refreshToken;
    }

    // Deletes a refresh token by its raw token value.
    public void deleteByToken(String token) {
        if (refreshTokenRepository.findByToken(token).isEmpty()) {
            throw new IllegalStateException("Refresh token not found.");
        }
        refreshTokenRepository.deleteByToken(token);
    }

    // Deletes a refresh token only if it currently exists.
    public void deleteIfPresent(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    // Deletes the refresh token belonging to the given username.
    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUser_Name(username);
    }

    // Finds the refresh token currently assigned to the given username.
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByUsername(String username) {
        return refreshTokenRepository.findByUser_Name(username);
    }
}
