package com.rkamradt.profileauth.controller;

import com.rkamradt.profileauth.config.AppProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProtectedController {

    private final AppProperties appProperties;

    public ProtectedController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/api/protected")
    public ResponseEntity<Map<String, String>> protectedData() {
        Map<String, String> data = appProperties.getProtectedData();
        if (data.isEmpty()) {
            return ResponseEntity.status(503).body(Map.of("error", "No protected data configured"));
        }
        return ResponseEntity.ok(data);
    }
}
