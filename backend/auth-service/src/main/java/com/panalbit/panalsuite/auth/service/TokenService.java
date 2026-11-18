package com.panalbit.panalsuite.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Token firmado sin estado: base64url("userId:expiraEpoch") + "." + base64url(HMAC-SHA256).
 * Version simple para uso academico (no reemplaza a un JWT real).
 */
@Service
public class TokenService {

    private final byte[] secret;
    private final long ttlSeconds;

    public TokenService(@Value("${panalsuite.auth.token-secret}") String secret,
                        @Value("${panalsuite.auth.token-ttl-hours}") long ttlHours) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlHours * 3600;
    }

    public String issue(long userId) {
        return issue(userId, Instant.now().getEpochSecond() + ttlSeconds);
    }

    public String issue(long userId, long expiresAtEpoch) {
        String payload = userId + ":" + expiresAtEpoch;
        String encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + encode(sign(encodedPayload));
    }

    /** Devuelve el id de usuario si el token es valido y no ha expirado. */
    public Optional<Long> validate(String token) {
        if (token == null) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return Optional.empty();
        }
        byte[] expected = sign(parts[0]);
        byte[] received;
        try {
            received = Base64.getUrlDecoder().decode(parts[1]);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
        if (!MessageDigest.isEqual(expected, received)) {
            return Optional.empty();
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] data = payload.split(":");
            long userId = Long.parseLong(data[0]);
            long expiresAt = Long.parseLong(data[1]);
            return Instant.now().getEpochSecond() <= expiresAt ? Optional.of(userId) : Optional.empty();
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo firmar el token", ex);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
