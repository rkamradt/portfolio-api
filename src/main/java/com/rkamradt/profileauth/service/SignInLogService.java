package com.rkamradt.profileauth.service;

import com.rkamradt.profileauth.model.SignInEvent;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignInLogService {

    private final MongoTemplate mongoTemplate;

    public SignInLogService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private String collectionName(String serviceName) {
        return serviceName + "_sign_ins";
    }

    public void log(String email, String name, String serviceName) {
        SignInEvent event = SignInEvent.of(email, name, serviceName);
        mongoTemplate.save(event, collectionName(serviceName));
    }

    public List<SignInEvent> findRecent(String serviceName, int limit) {
        Query query = new Query()
            .with(Sort.by(Sort.Direction.DESC, "timestamp"))
            .limit(limit);
        return mongoTemplate.find(query, SignInEvent.class, collectionName(serviceName));
    }
}
