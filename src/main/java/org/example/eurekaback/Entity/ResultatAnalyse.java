package org.example.eurekaback.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eurekaback.Dto.SentimentAnalysisData;
import org.example.eurekaback.Dto.SentimentAnalysisSummary;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "resultatanalyse")
public class ResultatAnalyse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ElementCollection
    private Map<String, Float> resultats = new HashMap<>();
    
    // Sentiment Analysis Fields
    @Column(name = "sentiment_analysis_data")
    @JdbcTypeCode(SqlTypes.JSON)
    private SentimentAnalysisData sentimentAnalysisData;
    
    @Column(name = "sentiment_analysis_summary")
    @JdbcTypeCode(SqlTypes.JSON)
    private SentimentAnalysisSummary sentimentAnalysisSummary;
    
    @Column(name = "sentiment_last_updated")
    private LocalDateTime sentimentLastUpdated;
    
    @Column(name = "sentiment_total_feedbacks")
    private Integer sentimentTotalFeedbacks = 0;
    
    @Column(name = "sentiment_analyzed_feedbacks")
    private Integer sentimentAnalyzedFeedbacks = 0;
    
    // One-to-one relationship with Campagne
    @OneToOne(mappedBy = "resultatAnalyse", fetch = FetchType.LAZY)
    private Campagne campagne;
    
    // Direct getters and setters for JSON fields (no manual serialization needed)
    public SentimentAnalysisSummary getSentimentSummary() {
        return sentimentAnalysisSummary;
    }
    
    public void setSentimentSummary(SentimentAnalysisSummary summary) {
        this.sentimentAnalysisSummary = summary;
    }
    
    public SentimentAnalysisData getSentimentData() {
        return sentimentAnalysisData;
    }
    
    public void setSentimentData(SentimentAnalysisData data) {
        this.sentimentAnalysisData = data;
    }
}
