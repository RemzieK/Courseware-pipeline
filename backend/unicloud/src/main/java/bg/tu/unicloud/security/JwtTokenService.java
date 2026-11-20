package bg.tu.unicloud.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JwtTokenService {

    private final String issuer;
    private final String audience;
    private final byte[] sharedSecret;

    public JwtTokenService(
            @Value("${unicloud.auth.issuer:https://auth.unicloud.tu-sofia.bg}") String issuer,
            @Value("${unicloud.auth.audience:unicloud-api}") String audience,
            @Value("${unicloud.auth.jwt-secret:unicloud-dev-jwt-super-secret-key-at-least-256-bits-long!}") String secret) {
        this.issuer = issuer;
        this.audience = audience;
        try {
            // SHA-256 digest ensures 256 bits for HMAC-SHA256
            this.sharedSecret = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize JWT secret digest", e);
        }
    }

    /**
     * Creates a signed JWT token with the standard claims.
     */
    public String createToken(String sub, String email, String name, String role, List<Long> courses, long durationSeconds) {
        try {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + (durationSeconds * 1000));

            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .audience(audience)
                    .subject(sub)
                    .claim("email", email)
                    .claim("name", name)
                    .claim("role", role)
                    .claim("courses", courses != null ? courses : List.of())
                    .issueTime(now)
                    .expirationTime(expiry);

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), builder.build());
            signedJWT.sign(new MACSigner(sharedSecret));
            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("Error creating JWT token", e);
            throw new RuntimeException("Error generating token", e);
        }
    }

    /**
     * Parses and validates a JWT token string, verifying signature and expiry.
     * Returns claims as a Map.
     */
    public Map<String, Object> parseAndValidateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // If signed with HMAC-SHA256, verify signature
            if (JWSAlgorithm.Family.HMAC_SHA.contains(signedJWT.getHeader().getAlgorithm())) {
                boolean verified = signedJWT.verify(new MACVerifier(sharedSecret));
                if (!verified) {
                    log.warn("JWT signature verification failed");
                    return null;
                }
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                log.warn("JWT token has expired");
                return null;
            }

            return claimsSet.getClaims();
        } catch (Exception e) {
            log.warn("Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }
}
