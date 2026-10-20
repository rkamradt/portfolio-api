package com.rkamradt.profileauth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.rkamradt.profileauth.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleTokenServiceTest {

    @Mock
    private AppProperties appProperties;

    private GoogleTokenService service;

    @BeforeEach
    void setUp() {
        when(appProperties.getGoogleClientId()).thenReturn("test-client-id");
        service = new GoogleTokenService(appProperties);
    }

    @Test
    void validToken_withVerifiedEmail_returnsPayload() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("user@example.com");
        payload.setEmailVerified(true);

        GoogleIdToken token = mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);

        GoogleIdToken.Payload result = service.verify("valid-credential", cred -> token);

        assertThat(result.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void nullFromVerifier_throwsUnauthorized() {
        assertThatThrownBy(() -> service.verify("bad-credential", cred -> null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Invalid token");
    }

    @Test
    void emailNotVerified_throwsUnauthorized() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("user@example.com");
        payload.setEmailVerified(false);

        GoogleIdToken token = mock(GoogleIdToken.class);
        when(token.getPayload()).thenReturn(payload);

        assertThatThrownBy(() -> service.verify("unverified-credential", cred -> token))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Email not verified");
    }

    @Test
    void wrongAudience_verifierReturnsNull_throwsUnauthorized() {
        assertThatThrownBy(() -> service.verify("wrong-audience-credential", cred -> null))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Invalid token");
    }

    @Test
    void verifierThrowsException_throwsUnauthorized() {
        assertThatThrownBy(() -> service.verify("bad-credential",
                cred -> { throw new RuntimeException("network error"); }))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Invalid token");
    }
}
