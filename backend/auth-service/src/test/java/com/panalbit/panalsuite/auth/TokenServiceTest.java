package com.panalbit.panalsuite.auth;

import com.panalbit.panalsuite.auth.service.TokenService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private final TokenService service = new TokenService("secreto-de-prueba", 1);

    @Test
    void issuedTokenIsValidAndReturnsUserId() {
        String token = service.issue(42L);
        assertEquals(42L, service.validate(token).orElseThrow());
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = service.issue(42L);
        assertTrue(service.validate(token + "x").isEmpty());
        assertTrue(service.validate("abc.def").isEmpty());
        assertTrue(service.validate("sin-punto").isEmpty());
        assertTrue(service.validate(null).isEmpty());
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        TokenService other = new TokenService("otro-secreto", 1);
        assertTrue(service.validate(other.issue(1L)).isEmpty());
    }

    @Test
    void expiredTokenIsRejected() {
        String expired = service.issue(7L, Instant.now().getEpochSecond() - 10);
        assertTrue(service.validate(expired).isEmpty());
    }
}
