package com.rkamradt.profileauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.rkamradt.profileauth.config.AppProperties;
import com.rkamradt.profileauth.config.SecurityConfig;
import com.rkamradt.profileauth.config.WebConfig;
import com.rkamradt.profileauth.service.GoogleTokenService;
import com.rkamradt.profileauth.service.SignInLogService;
import com.rkamradt.profileauth.web.RateLimitInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, ProtectedController.class})
@Import({SecurityConfig.class, WebConfig.class, RateLimitInterceptor.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoogleTokenService googleTokenService;

    @MockBean
    private SignInLogService signInLogService;

    @MockBean
    private AppProperties appProperties;

    @Test
    void googleSignIn_validCredential_returns200AndSessionCookie() throws Exception {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("user@example.com");
        payload.set("name", "Test User");

        when(appProperties.getGoogleClientId()).thenReturn("test-client-id");
        when(appProperties.getServiceName()).thenReturn("portfolio");
        when(googleTokenService.verify(anyString())).thenReturn(payload);

        mockMvc.perform(post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("credential", "valid-token"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("user@example.com"))
            .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void googleSignIn_invalidCredential_returns401() throws Exception {
        when(googleTokenService.verify(anyString()))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        mockMvc.perform(post("/api/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("credential", "bad-token"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/protected"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_returns200AndClearsSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/auth/logout")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true));
    }
}
