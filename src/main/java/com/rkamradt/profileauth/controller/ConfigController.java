package com.rkamradt.profileauth.controller;

import com.rkamradt.profileauth.config.AppProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigController {

    private final AppProperties appProperties;

    public ConfigController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/api/config")
    public ResponseEntity<Map<String, String>> config() {
        return ResponseEntity.ok(Map.of("googleClientId", appProperties.getGoogleClientId()));
    }
}
