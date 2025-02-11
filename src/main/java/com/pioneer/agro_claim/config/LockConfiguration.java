package com.pioneer.agro_claim.config;

import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Configuration
public class LockConfiguration {
    private final Map<String, ReentrantLock> tenantLocks = new ConcurrentHashMap<>();

    public ReentrantLock getTenantLock(String tenantId) {
        return tenantLocks.computeIfAbsent(tenantId, k -> new ReentrantLock());
    }
}
