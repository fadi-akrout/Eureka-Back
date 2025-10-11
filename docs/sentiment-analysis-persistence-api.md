# Sentiment Analysis Persistence API Documentation

## Overview

This document describes the REST API endpoints for managing sentiment analysis data persistence in the Eureka backend system. The sentiment analysis results are stored in the database alongside campaign analysis data for long-term access and reporting.

## Base URL
```
/api/ai
```

## Authentication
All endpoints require authentication with roles:
- `ADMIN` - Full access to all campaign sentiment data
- `ANNONCEUR` - Access to own campaigns only

## Endpoints

### 1. Save Sentiment Analysis Results

**POST** `/save-sentiment-analysis`

Saves sentiment analysis results to the database for persistent storage.

#### Request Body
```json
{
  "campaignId": 123,
  "sentimentAnalysisData": {
    "feedbackSentiments": [
      {
        "feedbackId": 456,
        "sentiment": "POSITIVE",
        "confidenceScore": 0.92,
        "scores": {
          "positive": 0.85,
          "negative": 0.05,
          "neutral": 0.10
        },
        "insights": "Customer expresses high satisfaction",
        "analyzedAt": "2024-01-15T10:25:00Z",
        "textLength": 156,
        "keywords": ["excellent", "satisfied", "recommend"]
      }
    ],
    "batchAnalysis": {
      "totalBatches": 1,
      "successfulAnalyses": 25,
      "failedAnalyses": 2,
      "averageProcessingTime": "2.3 seconds",
      "startTime": "2024-01-15T10:20:00Z",
      "endTime": "2024-01-15T10:30:00Z"
    }
  },
  "sentimentAnalysisSummary": {
    "overallSentiment": "POSITIVE",
    "sentimentDistribution": {
      "positive": 15,
      "negative": 3,
      "neutral": 7
    },
    "sentimentPercentages": {
      "positive": 60.0,
      "negative": 12.0,
      "neutral": 28.0
    },
    "averageConfidence": 0.85,
    "confidenceLevel": "HIGH",
    "keyInsights": [
      "Users appreciate the product quality and design",
      "Some concerns about pricing mentioned"
    ],
    "sentimentTrends": {
      "mostPositiveAspects": ["quality", "design", "usability"],
      "mostNegativeAspects": ["price", "delivery"],
      "neutralAspects": ["packaging", "support"]
    },
    "analysisMetadata": {
      "aiModel": "gemini-1.5-flash",
      "analysisDate": "2024-01-15T10:30:00Z",
      "processingTime": "45.2 seconds",
      "apiVersion": "v1"
    }
  },
  "totalFeedbacks": 27,
  "analyzedFeedbacks": 25
}
```

#### Response
```json
{
  "success": true,
  "message": "Sentiment analysis saved successfully",
  "campaignId": 123,
  "resultatAnalyseId": 456,
  "savedAt": "2024-01-15T10:30:00Z"
}
```

### 2. Retrieve Campaign Sentiment Analysis

**GET** `/campaign/{id}/sentiment-analysis`

Retrieves stored sentiment analysis results for a specific campaign.

#### Path Parameters
- `id` (Integer) - Campaign ID

#### Response
```json
{
  "success": true,
  "campaignId": 123,
  "sentimentAnalysisData": { /* Same structure as save request */ },
  "sentimentAnalysisSummary": { /* Same structure as save request */ },
  "lastUpdated": "2024-01-15T10:30:00Z",
  "totalFeedbacks": 27,
  "analyzedFeedbacks": 25,
  "campaignName": "Summer Product Launch"
}
```

### 3. Check Sentiment Analysis Existence

**GET** `/campaign/{id}/sentiment-analysis/exists`

Checks if sentiment analysis data exists for a campaign.

#### Path Parameters
- `id` (Integer) - Campaign ID

#### Response
```json
{
  "campaignId": 123,
  "hasSentimentAnalysis": true,
  "timestamp": 1642246800000
}
```

### 4. Delete Sentiment Analysis

**DELETE** `/campaign/{id}/sentiment-analysis`

Deletes stored sentiment analysis data for a campaign.

#### Path Parameters
- `id` (Integer) - Campaign ID

#### Response
```json
{
  "success": true,
  "campaignId": 123,
  "message": "Sentiment analysis deleted successfully",
  "timestamp": 1642246800000
}
```

### 5. Analyze Campaign Feedbacks

**POST** `/campaign/{id}/analyze-all-feedbacks`

Analyzes all feedbacks for a campaign and saves the results automatically.

#### Path Parameters
- `id` (Integer) - Campaign ID

#### Response
```json
{
  "success": true,
  "message": "Sentiment analysis saved successfully",
  "campaignId": 123,
  "resultatAnalyseId": 456,
  "savedAt": "2024-01-15T10:30:00Z"
}
```

### 6. Re-analyze Campaign Feedbacks

**POST** `/campaign/{id}/reanalyze-feedbacks`

Re-analyzes all feedbacks for a campaign, replacing existing analysis.

#### Path Parameters
- `id` (Integer) - Campaign ID

#### Response
```json
{
  "success": true,
  "message": "Sentiment analysis saved successfully",
  "campaignId": 123,
  "resultatAnalyseId": 456,
  "savedAt": "2024-01-15T10:30:00Z"
}
```

### 7. Get Campaign Analysis Info

**GET** `/campaign/{id}/analysis-info`

Gets information about campaign's readiness for sentiment analysis.

#### Path Parameters
- `id` (Integer) - Campaign ID

#### Response
```json
{
  "campaignId": 123,
  "hasFeedbacks": true,
  "feedbackCount": 27,
  "hasExistingAnalysis": true,
  "canAnalyze": true,
  "timestamp": 1642246800000
}
```

## Error Responses

All endpoints return standard error responses when something goes wrong:

```json
{
  "success": false,
  "errorMessage": "Campaign not found with ID: 123"
}
```

Common HTTP status codes:
- `200 OK` - Success
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Campaign or analysis not found
- `500 Internal Server Error` - Server error

## Database Schema

The sentiment analysis data is stored in the `resultat_analyse` table with these additional columns:

| Column | Type | Description |
|--------|------|-------------|
| `sentiment_analysis_data` | JSON | Detailed sentiment analysis results |
| `sentiment_analysis_summary` | JSON | Summary statistics and insights |
| `sentiment_last_updated` | TIMESTAMP | When analysis was last performed |
| `sentiment_total_feedbacks` | INT | Total number of feedbacks |
| `sentiment_analyzed_feedbacks` | INT | Number of analyzed feedbacks |

## Usage Examples

### JavaScript/Frontend Integration

```javascript
// Save sentiment analysis results
async function saveSentimentAnalysis(campaignId, analysisData) {
  const response = await fetch('/api/ai/save-sentiment-analysis', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({
      campaignId: campaignId,
      sentimentAnalysisData: analysisData.data,
      sentimentAnalysisSummary: analysisData.summary,
      totalFeedbacks: analysisData.totalFeedbacks,
      analyzedFeedbacks: analysisData.analyzedFeedbacks
    })
  });
  
  return await response.json();
}

// Retrieve sentiment analysis
async function getCampaignSentiment(campaignId) {
  const response = await fetch(`/api/ai/campaign/${campaignId}/sentiment-analysis`, {
    headers: {
      'Authorization': 'Bearer ' + token
    }
  });
  
  return await response.json();
}

// Analyze campaign feedbacks
async function analyzeCampaignFeedbacks(campaignId) {
  const response = await fetch(`/api/ai/campaign/${campaignId}/analyze-all-feedbacks`, {
    method: 'POST',
    headers: {
      'Authorization': 'Bearer ' + token
    }
  });
  
  return await response.json();
}
```

## Security Considerations

1. **Authorization**: All endpoints check user roles and campaign ownership
2. **Data Validation**: Request payloads are validated for structure and content
3. **Error Handling**: Sensitive information is not exposed in error messages
4. **Logging**: All operations are logged for audit purposes

## Performance Notes

1. **Indexing**: Database indexes are created on sentiment analysis timestamps
2. **JSON Storage**: Efficient JSON storage for flexible data structures
3. **Batch Processing**: Large campaigns are processed in manageable batches
4. **Caching**: Consider implementing caching for frequently accessed results