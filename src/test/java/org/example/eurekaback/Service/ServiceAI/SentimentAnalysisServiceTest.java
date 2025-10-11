package org.example.eurekaback.Service.ServiceAI;

import org.example.eurekaback.Dto.SentimentAnalysisRequestDTO;
import org.example.eurekaback.Dto.SentimentAnalysisResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SentimentAnalysisServiceTest {
    
    // This is a placeholder test - you can implement actual tests when needed
    @Test
    void testSentimentAnalysisDTO() {
        // Test DTO creation
        SentimentAnalysisRequestDTO request = new SentimentAnalysisRequestDTO();
        request.setText("This is a great product! I love it!");
        
        assertNotNull(request.getText());
        assertEquals("This is a great product! I love it!", request.getText());
        
        // Test response DTO
        SentimentAnalysisResponseDTO.SentimentScores scores = 
            new SentimentAnalysisResponseDTO.SentimentScores(0.8, 0.1, 0.1);
        
        SentimentAnalysisResponseDTO response = 
            new SentimentAnalysisResponseDTO("POSITIVE", 0.85, scores, "Very positive feedback");
        
        assertTrue(response.isSuccess());
        assertEquals("POSITIVE", response.getSentiment());
        assertEquals(0.85, response.getConfidenceScore());
        assertNotNull(response.getScores());
    }
}