package com.rkamradt.profileauth.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppProperties {

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Value("${OWNER_GOOGLE_EMAIL}")
    private String ownerGoogleEmail;

    @Value("${SERVICE_NAME:portfolio}")
    private String serviceName;

    private Map<String, String> protectedData = new HashMap<>();

    @PostConstruct
    public void init() {
        System.getenv().forEach((key, value) -> {
            if (key.startsWith("PROTECTED_")) {
                String shortKey = key.substring("PROTECTED_".length()).toLowerCase();
                protectedData.put(shortKey, value);
            }
        });
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public String getOwnerGoogleEmail() {
        return ownerGoogleEmail;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Map<String, String> getProtectedData() {
        return protectedData;
    }
}
