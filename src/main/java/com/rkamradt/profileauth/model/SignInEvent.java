package com.rkamradt.profileauth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
public class SignInEvent {

    @Id
    private String id;
    private String email;
    private String name;
    private Instant timestamp;
    private String serviceName;

    public SignInEvent() {
    }

    public SignInEvent(String email, String name, Instant timestamp, String serviceName) {
        this.email = email;
        this.name = name;
        this.timestamp = timestamp;
        this.serviceName = serviceName;
    }

    public static SignInEvent of(String email, String name, String serviceName) {
        return new SignInEvent(email, name, Instant.now(), serviceName);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
}
