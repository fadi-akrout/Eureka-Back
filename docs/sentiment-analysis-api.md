# Sentiment Analysis API Documentation

## Overview
The Sentiment Analysis API allows you to analyze the emotional tone of text feedback for campaigns. It uses Google's Gemini AI to provide accurate sentiment classification with confidence scores.

## Endpoint

### POST `/api/ai/analyze-sentiment`

Analyzes the sentiment of the provided text and returns detailed sentiment information.

#### Authentication
- Requires authentication
- Allowed roles: `ADMIN`, `ANNONCEUR`

#### Request Body
```json
{
  "text": "This campaign is absolutely fantastic! I love how engaging it is.",
  "campaignId": 123,
  "context": "Customer feedback on summer campaign"
}
```

#### Request Parameters
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| text | String | Yes | Text to analyze (max 5000 characters) |
| campaignId | Long | No | Campaign ID for tracking purposes |
| context | String | No | Additional context for better analysis |

#### Response Body (Success)
```json
{
  "success": true,
  "sentiment": "POSITIVE",
  "confidenceScore": 0.92,
  "scores": {
    "positive": 0.85,
    "negative": 0.05,
    "neutral": 0.10
  },
  "insights": "The text expresses strong positive emotions with words like 'fantastic' and 'love', indicating high customer satisfaction.",
  "errorMessage": null
}
```

#### Response Body (Error)
```json
{
  "success": false,
  "sentiment": null,
  "confidenceScore": null,
  "scores": null,
  "insights": null,
  "errorMessage": "Internal server error: API rate limit exceeded"
}
```

#### Response Fields
| Field | Type | Description |
|-------|------|-------------|
| success | Boolean | Whether the analysis was successful |
| sentiment | String | POSITIVE, NEGATIVE, or NEUTRAL |
| confidenceScore | Double | Confidence level (0.0 to 1.0) |
| scores | Object | Detailed sentiment scores |
| scores.positive | Double | Positive sentiment score (0.0 to 1.0) |
| scores.negative | Double | Negative sentiment score (0.0 to 1.0) |
| scores.neutral | Double | Neutral sentiment score (0.0 to 1.0) |
| insights | String | AI-generated insights about the sentiment |
| errorMessage | String | Error description if analysis failed |

#### HTTP Status Codes
- `200 OK`: Analysis completed successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `500 Internal Server Error`: Service error

## Usage Examples

### Frontend JavaScript/TypeScript
```javascript
const analyzeSentiment = async (feedbackText, campaignId) => {
  try {
    const response = await fetch('/api/ai/analyze-sentiment', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify({
        text: feedbackText,
        campaignId: campaignId,
        context: "Campaign feedback analysis"
      })
    });
    
    const result = await response.json();
    
    if (result.success) {
      console.log(`Sentiment: ${result.sentiment}`);
      console.log(`Confidence: ${(result.confidenceScore * 100).toFixed(1)}%`);
      console.log(`Insights: ${result.insights}`);
    } else {
      console.error('Analysis failed:', result.errorMessage);
    }
    
    return result;
  } catch (error) {
    console.error('Network error:', error);
    throw error;
  }
};

// Usage
analyzeSentiment("This product exceeded my expectations!", 123);
```

### cURL Example
```bash
curl -X POST http://localhost:8080/api/ai/analyze-sentiment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "text": "I am really disappointed with this campaign. It does not meet my expectations.",
    "campaignId": 456,
    "context": "Post-campaign customer feedback"
  }'
```

## Integration Tips

### Frontend Integration
1. **Feedback Forms**: Integrate with campaign feedback forms to automatically analyze user responses
2. **Real-time Analysis**: Show sentiment indicators as users type their feedback
3. **Batch Processing**: Analyze multiple feedback entries at once for campaign reports
4. **Dashboard Visualization**: Display sentiment trends and insights on admin dashboards

### Error Handling
```javascript
const handleSentimentAnalysis = async (text) => {
  try {
    const result = await analyzeSentiment(text);
    
    if (!result.success) {
      // Handle API errors
      if (result.errorMessage.includes("rate limit")) {
        showMessage("Analysis temporarily unavailable. Please try again later.");
      } else {
        showMessage("Unable to analyze sentiment. Please try again.");
      }
      return null;
    }
    
    return result;
  } catch (error) {
    // Handle network errors
    showMessage("Network error. Please check your connection.");
    return null;
  }
};
```

### Performance Considerations
- Text length limit: 5000 characters
- API timeout: 30 seconds
- Rate limiting may apply based on your Gemini API quota
- Consider caching results for identical text inputs

## Health Check

### GET `/api/ai/health`
Check the availability of AI services including sentiment analysis.

#### Response
```json
{
  "status": "UP",
  "services": {
    "textGeneration": "UP",
    "sentimentAnalysis": "UP"
  },
  "provider": "Gemini AI",
  "timestamp": 1704067200000
}
```