package org.example.eurekaback.Service.ServiceAI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Config.GeminiConfig;
import org.example.eurekaback.Dto.SentimentAnalysisRequestDTO;
import org.example.eurekaback.Dto.SentimentAnalysisResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Slf4j
public class GeminiSentimentAnalysisServiceImpl implements SentimentAnalysisService {
    
    private final WebClient geminiWebClient;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;
    
    @Override
    public SentimentAnalysisResponseDTO analyzeSentiment(SentimentAnalysisRequestDTO request) {
        try {
            String prompt = buildSentimentPrompt(request);
            String response = callGeminiAPI(prompt);
            return parseSentimentResponse(response);
            
        } catch (Exception e) {
            log.error("Error analyzing sentiment: {}", e.getMessage(), e);
            return new SentimentAnalysisResponseDTO("Failed to analyze sentiment: " + e.getMessage(), true);
        }
    }
    
    @Override
    public boolean isServiceAvailable() {
        try {
            SentimentAnalysisRequestDTO testRequest = new SentimentAnalysisRequestDTO();
            testRequest.setText("This is a test message.");
            analyzeSentiment(testRequest);
            return true;
        } catch (Exception e) {
            log.warn("Sentiment analysis service health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private String buildSentimentPrompt(SentimentAnalysisRequestDTO request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Analyze the sentiment of the following text and provide a detailed analysis.\n\n");
        prompt.append("Text to analyze: \"").append(request.getText()).append("\"\n\n");
        
        if (request.getContext() != null && !request.getContext().trim().isEmpty()) {
            prompt.append("Context: ").append(request.getContext()).append("\n\n");
        }
        
        prompt.append("Please provide your response in the following JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"sentiment\": \"POSITIVE\", // POSITIVE, NEGATIVE, or NEUTRAL\n");
        prompt.append("  \"confidence\": 0.85, // confidence score between 0.0 and 1.0\n");
        prompt.append("  \"scores\": {\n");
        prompt.append("    \"positive\": 0.75,\n");
        prompt.append("    \"negative\": 0.15,\n");
        prompt.append("    \"neutral\": 0.10\n");
        prompt.append("  },\n");
        prompt.append("  \"insights\": \"Brief explanation of the sentiment analysis\"\n");
        prompt.append("}\n\n");
        prompt.append("Analyze the emotional tone, context, and overall sentiment. ");
        prompt.append("Consider nuances like sarcasm, irony, or mixed emotions. ");
        prompt.append("Provide specific insights about what makes the text positive, negative, or neutral.");
        
        return prompt.toString();
    }
    
    private SentimentAnalysisResponseDTO parseSentimentResponse(String response) {
        try {
            // Extract JSON from the response if it's wrapped in markdown or other text
            String jsonStr = extractJsonFromResponse(response);
            
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            String sentiment = jsonNode.path("sentiment").asText().toUpperCase();
            Double confidence = jsonNode.path("confidence").asDouble();
            String insights = jsonNode.path("insights").asText();
            
            // Parse scores
            JsonNode scoresNode = jsonNode.path("scores");
            SentimentAnalysisResponseDTO.SentimentScores scores = new SentimentAnalysisResponseDTO.SentimentScores(
                scoresNode.path("positive").asDouble(),
                scoresNode.path("negative").asDouble(),
                scoresNode.path("neutral").asDouble()
            );
            
            // Validate sentiment value
            if (!sentiment.matches("POSITIVE|NEGATIVE|NEUTRAL")) {
                sentiment = "NEUTRAL";
                confidence = 0.5;
            }
            
            // Validate confidence score
            if (confidence < 0.0 || confidence > 1.0) {
                confidence = 0.5;
            }
            
            return new SentimentAnalysisResponseDTO(sentiment, confidence, scores, insights);
            
        } catch (Exception e) {
            log.error("Error parsing sentiment response: {}", response, e);
            // Fallback: Try to extract sentiment using simple text analysis
            return fallbackSentimentAnalysis(response);
        }
    }
    
    private String extractJsonFromResponse(String response) {
        // Try to find JSON within the response
        Pattern jsonPattern = Pattern.compile("\\{[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}", Pattern.DOTALL);
        Matcher matcher = jsonPattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        // If no JSON found, return the original response
        return response;
    }
    
    private SentimentAnalysisResponseDTO fallbackSentimentAnalysis(String response) {
        String sentiment = "NEUTRAL";
        Double confidence = 0.5;
        String insights = "Unable to parse detailed analysis, using fallback sentiment detection.";
        
        // Simple keyword-based fallback
        String lowerResponse = response.toLowerCase();
        if (lowerResponse.contains("positive") || lowerResponse.contains("good") || lowerResponse.contains("happy")) {
            sentiment = "POSITIVE";
            confidence = 0.6;
        } else if (lowerResponse.contains("negative") || lowerResponse.contains("bad") || lowerResponse.contains("sad")) {
            sentiment = "NEGATIVE";
            confidence = 0.6;
        }
        
        SentimentAnalysisResponseDTO.SentimentScores scores = new SentimentAnalysisResponseDTO.SentimentScores(
            sentiment.equals("POSITIVE") ? 0.6 : 0.2,
            sentiment.equals("NEGATIVE") ? 0.6 : 0.2,
            sentiment.equals("NEUTRAL") ? 0.6 : 0.2
        );
        
        return new SentimentAnalysisResponseDTO(sentiment, confidence, scores, insights);
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
        
        // Add generation config optimized for sentiment analysis
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.3); // Lower temperature for more consistent results
        generationConfig.put("topK", 20);
        generationConfig.put("topP", 0.8);
        generationConfig.put("maxOutputTokens", 512); // Smaller response for sentiment analysis
        
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