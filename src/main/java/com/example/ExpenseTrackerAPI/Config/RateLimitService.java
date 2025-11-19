package com.example.ExpenseTrackerAPI.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String userId) {
        return cache.computeIfAbsent(userId, this::newBucket);
    }

    private Bucket newBucket(String userId) {
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(10, Duration.ofSeconds(15)));

        return Bucket.builder().addLimit(limit).build();
    }
}
