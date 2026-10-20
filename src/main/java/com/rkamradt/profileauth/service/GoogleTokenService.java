package com.rkamradt.profileauth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.rkamradt.profileauth.config.AppProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GoogleTokenService {

    /**
     * Functional interface extracted so tests can inject a lambda instead of
     * constructing a real GoogleIdTokenVerifier (which makes network calls).
     */
    @FunctionalInterface
    public interface CredentialVerifier {
        GoogleIdToken verify(String credential) throws Exception;
    }

    private final AppProperties appProperties;
    private volatile CredentialVerifier verifier;

    public GoogleTokenService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /** Production path — lazily build a real GoogleIdTokenVerifier. */
    private CredentialVerifier getVerifier() {
        if (verifier == null) {
            synchronized (this) {
                if (verifier == null) {
                    GoogleIdTokenVerifier gv = new GoogleIdTokenVerifier.Builder(
                            new NetHttpTransport(), GsonFactory.getDefaultInstance())
                            .setAudience(List.of(appProperties.getGoogleClientId()))
                            .build();
                    verifier = gv::verify;
                }
            }
        }
        return verifier;
    }

    /**
     * Verify a Google One Tap credential string and return the payload.
     * Throws ResponseStatusException(401) for any invalid/expired/unverified token.
     */
    public GoogleIdToken.Payload verify(String credential) {
        return verify(credential, getVerifier());
    }

    /**
     * Package-private overload used by unit tests to inject a mock verifier.
     */
    GoogleIdToken.Payload verify(String credential, CredentialVerifier credentialVerifier) {
        try {
            GoogleIdToken token = credentialVerifier.verify(credential);
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }
            GoogleIdToken.Payload payload = token.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not verified");
            }
            return payload;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }
}
