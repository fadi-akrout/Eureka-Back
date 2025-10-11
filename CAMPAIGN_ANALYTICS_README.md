# Campaign Analytics System

## Overview

The Campaign Analytics System provides comprehensive analysis of campaign sentiment data, generating advanced insights, trends, and recommendations based on existing feedback and sentiment analysis data.

## Features

### 1. Sentiment Distribution Analysis
- **Positive/Negative/Neutral counts and percentages**
- **Overall sentiment determination**
- **Statistical significance indicators**

### 2. Confidence Score Analysis
- **Average confidence levels**
- **High/Medium/Low confidence distribution**
- **Confidence-based reliability indicators**

### 3. Advanced Insights Generation
- **Key Findings**: Significant patterns in sentiment data
- **Recommendations**: Actionable suggestions for campaign improvement
- **Trends**: Confidence and sentiment trends analysis
- **Risk Factors**: Potential issues and concerns identification

### 4. Keyword Analysis
- **Frequency-based keyword extraction**
- **Sentiment association for each keyword**
- **Confidence scoring for keyword reliability**

### 5. Daily Trends Analysis
- **Time-based sentiment progression**
- **Daily response counts and patterns**
- **Confidence trends over time**

### 6. Additional Metrics
- **Response rates and completion rates**
- **Average ratings**
- **Participation statistics**

## API Endpoints

### 1. Generate Full Analytics (POST)
```http
POST /api/campaign-analytics/generate
Content-Type: application/json

{
    "campaignId": 123,
    "includeKeywords": true,
    "includeDailyTrends": true,
    "forceRefresh": false,
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "maxKeywords": 20
}
```

### 2. Get Analytics (GET)
```http
GET /api/campaign-analytics/123?includeKeywords=true&includeDailyTrends=true&maxKeywords=20
```

### 3. Get Analytics Overview (GET)
```http
GET /api/campaign-analytics/123/overview
```

### 4. Health Check
```http
GET /api/campaign-analytics/health
```

## Request Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `campaignId` | Integer | Required | Campaign ID to analyze |
| `includeKeywords` | Boolean | true | Include keyword analysis |
| `includeDailyTrends` | Boolean | true | Include daily trend analysis |
| `forceRefresh` | Boolean | false | Force recalculation (future use) |
| `startDate` | String | null | Start date for trend analysis (YYYY-MM-DD) |
| `endDate` | String | null | End date for trend analysis (YYYY-MM-DD) |
| `maxKeywords` | Integer | 20 | Maximum number of keywords to return |

## Response Structure

```json
{
    "success": true,
    "message": "Analytics generated successfully",
    "data": {
        "campaignId": 123,
        "campaignName": "Sample Campaign",
        "sentimentDistribution": {
            "positive": 45,
            "negative": 15,
            "neutral": 20,
            "totalResponses": 80,
            "analyzedResponses": 80,
            "positivePercentage": 56.25,
            "negativePercentage": 18.75,
            "neutralPercentage": 25.00,
            "overallSentiment": "POSITIVE"
        },
        "confidenceScores": {
            "averageConfidence": 0.8234,
            "highConfidenceCount": 60,
            "mediumConfidenceCount": 15,
            "lowConfidenceCount": 5,
            "highConfidencePercentage": 75.00,
            "mediumConfidencePercentage": 18.75,
            "lowConfidencePercentage": 6.25,
            "confidenceLevel": "HIGH"
        },
        "insights": {
            "keyFindings": [
                {
                    "text": "Campaign shows overwhelmingly positive sentiment with 56.3% positive responses",
                    "confidenceScore": 0.9000,
                    "priorityLevel": "HIGH",
                    "sourceFeedbackCount": 45
                }
            ],
            "recommendations": [
                {
                    "text": "Consider addressing the concerns raised in negative feedback to improve campaign reception",
                    "confidenceScore": 0.8000,
                    "priorityLevel": "HIGH",
                    "sourceFeedbackCount": 15
                }
            ],
            "trends": [
                {
                    "text": "High confidence in sentiment analysis (82.3% average) indicates reliable feedback quality",
                    "confidenceScore": 0.8500,
                    "priorityLevel": "MEDIUM",
                    "sourceFeedbackCount": 60
                }
            ],
            "riskFactors": []
        },
        "additionalMetrics": {
            "responseRate": 80.00,
            "completionRate": 80.00,
            "averageRating": 4.2,
            "totalInvited": 100,
            "totalResponded": 80,
            "totalCompleted": 80
        },
        "topKeywords": [
            {
                "keyword": "quality",
                "frequency": 25,
                "sentimentAssociation": "POSITIVE",
                "confidenceScore": 0.8500
            }
        ],
        "dailyTrends": [
            {
                "date": "2024-01-15",
                "responseCount": 12,
                "averageRating": 0.00,
                "dominantSentiment": "POSITIVE",
                "positiveCount": 8,
                "negativeCount": 2,
                "neutralCount": 2,
                "averageConfidence": 0.8200
            }
        ],
        "generatedAt": "2024-01-15T10:30:00"
    }
}
```

## Prerequisites

### 1. Existing Data Requirements
- Campaign must exist in the database
- Campaign must have sentiment analysis data in `resultatanalyse` table
- Sentiment analysis data must include:
  - `sentimentAnalysisData` (JSON field with feedback sentiments)
  - `sentimentAnalysisSummary` (JSON field with summary data)

### 2. Security Requirements
- User must have `ADMIN` or `ANNONCEUR` role
- Authentication required for all endpoints

## Usage Examples

### 1. Basic Analytics Generation
```bash
curl -X GET "http://localhost:8080/api/campaign-analytics/123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Analytics with Custom Parameters
```bash
curl -X GET "http://localhost:8080/api/campaign-analytics/123?includeKeywords=true&includeDailyTrends=false&maxKeywords=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Quick Overview (No Keywords/Trends)
```bash
curl -X GET "http://localhost:8080/api/campaign-analytics/123/overview" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Full Analytics with POST
```bash
curl -X POST "http://localhost:8080/api/campaign-analytics/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "campaignId": 123,
    "includeKeywords": true,
    "includeDailyTrends": true,
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "maxKeywords": 15
  }'
```

## Error Handling

### Common Error Responses

#### Campaign Not Found
```json
{
    "success": false,
    "message": "Campaign not found with ID: 123",
    "data": null,
    "errorCode": null
}
```

#### No Sentiment Data
```json
{
    "success": false,
    "message": "No sentiment analysis data found for campaign ID: 123",
    "data": null,
    "errorCode": null
}
```

#### Invalid Parameters
```json
{
    "success": false,
    "message": "Campaign ID is required",
    "data": null,
    "errorCode": null
}
```

## Performance Considerations

### 1. Data Processing
- Analytics are computed on-demand from existing sentiment data
- No additional database tables required (optional caching tables available)
- Processing time depends on the number of feedback entries

### 2. Caching (Optional)
- Optional database tables provided for caching results
- `forceRefresh` parameter available for cache invalidation
- Consider implementing caching for campaigns with large datasets

### 3. Optimization Tips
- Use `overview` endpoint for quick summaries
- Set `includeKeywords=false` and `includeDailyTrends=false` for faster responses
- Limit `maxKeywords` parameter for better performance

## Integration with Existing System

### 1. Data Dependencies
- Uses existing `Campagne` entity
- Reads from `ResultatAnalyse` entity (sentiment data)
- Accesses `Feedback` and `PanelisteCampagne` for additional metrics

### 2. Security Integration
- Uses existing Spring Security configuration
- Respects existing role-based access control
- Compatible with existing JWT authentication

### 3. Service Layer Integration
- Follows existing service pattern
- Uses existing repository interfaces
- Compatible with existing transaction management

## Future Enhancements

### 1. Caching Implementation
- Implement optional database tables for result caching
- Add cache invalidation logic
- Performance optimization for large datasets

### 2. Real-time Analytics
- WebSocket integration for live updates
- Streaming analytics for ongoing campaigns
- Real-time dashboard support

### 3. Advanced Analytics
- Machine learning integration
- Predictive analytics
- Comparative analysis between campaigns

### 4. Export Capabilities
- PDF report generation
- Excel export functionality
- Scheduled report delivery

## Troubleshooting

### 1. No Analytics Data
**Problem**: "No sentiment analysis data found"
**Solution**: Ensure the campaign has been analyzed using the sentiment analysis service first

### 2. Empty Results
**Problem**: Analytics return empty or zero values
**Solution**: Check that sentiment analysis data contains valid feedback sentiments

### 3. Performance Issues
**Problem**: Slow response times
**Solution**: Use overview endpoint or disable keywords/trends for faster responses

### 4. Authentication Errors
**Problem**: 403 Forbidden responses
**Solution**: Ensure user has ADMIN or ANNONCEUR role and valid JWT token

## Support

For technical support or questions about the Campaign Analytics System:
1. Check the logs for detailed error messages
2. Verify data prerequisites are met
3. Test with the health check endpoint
4. Review the API documentation for correct parameter usage