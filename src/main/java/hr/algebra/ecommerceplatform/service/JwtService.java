package hr.algebra.ecommerceplatform.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:30m}")
    private Duration accessTokenExpiration;

    // Generates a signed JWT access token for the given username.
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration.toMillis()))
                .signWith(getSigningKey())
                .compact();
    }

    // Extracts the username stored in the token subject.
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Validates the token against the expected user and expiration time.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && extractAllClaims(token).getExpiration().after(new Date());
    }

    // Validates only the token structure and expiration time.
    public boolean isTokenValid(String token) {
        try {
            return extractAllClaims(token).getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    // Parses the token and returns all claims from its payload.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Builds the signing key used to sign and verify JWTs.
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
