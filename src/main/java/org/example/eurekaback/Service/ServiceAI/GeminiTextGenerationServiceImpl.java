package org.example.eurekaback.Service.ServiceAI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Config.GeminiConfig;
import org.example.eurekaback.Dto.AIGenerationRequestDTO;
import org.example.eurekaback.Dto.AIGenerationResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class GeminiTextGenerationServiceImpl implements AITextGenerationService {
    
    private final WebClient geminiWebClient;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;
    
    @Override
    public AIGenerationResponseDTO generateCampaignDescription(AIGenerationRequestDTO request) {
        try {
            String prompt = buildCampaignPrompt(request);
            String response = callGeminiAPI(prompt);
            return new AIGenerationResponseDTO(response);
            
        } catch (Exception e) {
            log.error("Error generating campaign description: {}", e.getMessage(), e);
            return new AIGenerationResponseDTO("Failed to generate campaign description: " + e.getMessage(), true);
        }
    }
    
    @Override
    public AIGenerationResponseDTO generateProductDescription(AIGenerationRequestDTO request) {
        try {
            String prompt = buildProductPrompt(request);
            String response = callGeminiAPI(prompt);
            return new AIGenerationResponseDTO(response);
            
        } catch (Exception e) {
            log.error("Error generating product description: {}", e.getMessage(), e);
            return new AIGenerationResponseDTO("Failed to generate product description: " + e.getMessage(), true);
        }
    }
    
    @Override
    public boolean isServiceAvailable() {
        try {
            String testPrompt = "Test";
            callGeminiAPI(testPrompt);
            return true;
        } catch (Exception e) {
            log.warn("Gemini AI service health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private String buildCampaignPrompt(AIGenerationRequestDTO request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Create a compelling marketing campaign description for: ")
              .append(request.getName());
              
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            prompt.append(" in the ").append(request.getCategory()).append(" category");
        }
        
        if (request.getTargetAudience() != null && !request.getTargetAudience().trim().isEmpty()) {
            prompt.append(". Target audience: ").append(request.getTargetAudience());
        }
        
        prompt.append(". The description should be engaging, highlight key benefits, and motivate participation.");
        
        if (request.getAdditionalContext() != null && !request.getAdditionalContext().trim().isEmpty()) {
            prompt.append(" Additional context: ").append(request.getAdditionalContext());
        }
        
        prompt.append(" Keep it under 300 words and use a professional tone.");
        
        return prompt.toString();
    }
    
    private String buildProductPrompt(AIGenerationRequestDTO request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Create a detailed product description for: ")
              .append(request.getName());
              
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            prompt.append(" in the ").append(request.getCategory()).append(" category");
        }
        
        if (request.getKeyFeatures() != null && !request.getKeyFeatures().trim().isEmpty()) {
            prompt.append(". Key features: ").append(request.getKeyFeatures());
        }
        
        prompt.append(". The description should be informative, highlight unique selling points, and appeal to potential customers.");
        
        if (request.getAdditionalContext() != null && !request.getAdditionalContext().trim().isEmpty()) {
            prompt.append(" Additional context: ").append(request.getAdditionalContext());
        }
        
        prompt.append(" Keep it under 250 words and use an engaging tone.");
        
        return prompt.toString();
    }
    
    private String callGeminiAPI(String prompt) throws Exception {
        Map<String, Object> requestBody = buildGeminiRequest(prompt);
        
        try {
            String response = geminiWebClient
                    .post()
                    .uri("/v1beta/models/" + geminiConfig.getDefaultModel() + ":generateContent?key=" + geminiConfig.getGeminiApiKey())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
                    
            return extractTextFromResponse(response);
            
        } catch (WebClientResponseException e) {
            log.error("Gemini API error: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new Exception("Gemini API error: " + e.getMessage());
        }
    }
    
    private Map<String, Object> buildGeminiRequest(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // Build the contents array
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", List.of(part));
        
        requestBody.put("contents", List.of(content));
        
        // Add generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topK", 40);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 1024);
        
        requestBody.put("generationConfig", generationConfig);
        
        return requestBody;
    }
    
    private String extractTextFromResponse(String response) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // Navigate through the Gemini response structure
            JsonNode candidates = jsonNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            
            throw new Exception("Unexpected response format from Gemini API");
            
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", response);
            throw new Exception("Failed to parse Gemini API response: " + e.getMessage());
        }
    }
}