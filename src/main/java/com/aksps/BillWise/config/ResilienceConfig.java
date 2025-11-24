package com.aksps.BillWise.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Global configuration for Resilience4j and WebClient.
 * Provides CircuitBreaker, Retry, and TimeLimiter policies
 * and defines a shared WebClient bean for ML API integration.
 */
@Configuration
public class ResilienceConfig {

    /**
     * Base URL for the external Python ML API.
     * Example: http://localhost:5000/api/ml
     */
    private static final String ML_API_BASE_URL = "http://localhost:5000/api/ml";

    /**
     * Configures a shared WebClient bean for non-blocking REST calls
     * used by MlIntegrationService and other external integrations.
     */
    @Bean
    public WebClient mlServiceWebClient() {
        return WebClient.builder()
                .baseUrl(ML_API_BASE_URL)
                .build();
    }

    /**
     * Default CircuitBreaker configuration.
     */
    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)                 // Open if 50% of calls fail
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .slidingWindowSize(10)                    // Evaluate over last 10 calls
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
    }

    /**
     * Default Retry configuration.
     */
    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .retryExceptions(RuntimeException.class)
                .build();
    }

    /**
     * Default TimeLimiter configuration.
     */
    @Bean
    public TimeLimiterConfig defaultTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();
    }
}
