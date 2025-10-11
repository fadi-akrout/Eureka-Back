package org.example.eurekaback.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.eurekaback.Dto.SentimentAnalysisData;
import org.example.eurekaback.Dto.SentimentAnalysisSummary;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentimentAnalyticsDTO {
    private Integer id;
    private SentimentAnalysisData sentimentAnalysisData;
    private SentimentAnalysisSummary sentimentAnalysisSummary;
    private LocalDateTime sentimentLastUpdated;
    private Integer sentimentTotalFeedbacks;
    private Integer sentimentAnalyzedFeedbacks;
}

