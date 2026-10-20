package com.rkamradt.profileauth.controller;

import com.rkamradt.profileauth.config.AppProperties;
import com.rkamradt.profileauth.model.SignInEvent;
import com.rkamradt.profileauth.service.SignInLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SignInLogService signInLogService;
    private final AppProperties appProperties;

    public AdminController(SignInLogService signInLogService, AppProperties appProperties) {
        this.signInLogService = signInLogService;
        this.appProperties = appProperties;
    }

    @GetMapping("/sign-ins")
    public ResponseEntity<?> signIns(Authentication authentication) {
        String authenticatedEmail = (String) authentication.getPrincipal();
        if (!appProperties.getOwnerGoogleEmail().equals(authenticatedEmail)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        List<SignInEvent> events = signInLogService.findRecent(appProperties.getServiceName(), 100);
        return ResponseEntity.ok(events);
    }
}
