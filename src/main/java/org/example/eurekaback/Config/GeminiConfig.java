package org.example.eurekaback.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiConfig {
    
    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;
    
    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com}")
    private String geminiBaseUrl;
    
    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String defaultModel;
    
    @Bean
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl(geminiBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
                .build();
    }
    
    public String getGeminiApiKey() {
        return geminiApiKey;
    }
    
    public String getDefaultModel() {
        return defaultModel;
    }
}