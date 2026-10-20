package com.rkamradt.profileauth.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.rkamradt.profileauth.config.AppProperties;
import com.rkamradt.profileauth.service.GoogleTokenService;
import com.rkamradt.profileauth.service.SignInLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GoogleTokenService googleTokenService;
    private final SignInLogService signInLogService;
    private final AppProperties appProperties;

    public AuthController(GoogleTokenService googleTokenService,
                          SignInLogService signInLogService,
                          AppProperties appProperties) {
        this.googleTokenService = googleTokenService;
        this.signInLogService = signInLogService;
        this.appProperties = appProperties;
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, String>> googleSignIn(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String credential = body.get("credential");
        GoogleIdToken.Payload payload = googleTokenService.verify(credential);

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        if (name == null) {
            name = email;
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            email, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContext context = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        signInLogService.log(email, name, appProperties.getServiceName());

        return ResponseEntity.ok(Map.of("email", email, "name", name));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
