# AI Text Generation Integration Guide

## Overview
This guide explains how to use the AI-powered text generation API for campaign and product descriptions using Google Gemini AI.

## Setup

### 1. Get Google Gemini API Key
1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create a new API key
3. Set the environment variable: `GEMINI_API_KEY=your-actual-api-key`

### 2. Configuration
The AI service is configured in `application.properties`:
```properties
ai.gemini.api-key=${GEMINI_API_KEY:your-gemini-api-key-here}
ai.gemini.base-url=https://generativelanguage.googleapis.com
ai.gemini.model=gemini-1.5-flash
```

## API Endpoints

### 1. General AI Endpoints

#### Generate Campaign Description
```http
POST /api/ai/generate-campaign-description
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "name": "Summer Sale Campaign",
  "category": "Fashion",
  "targetAudience": "Young adults aged 18-35",
  "additionalContext": "Focus on sustainability and eco-friendly products"
}
```

#### Generate Product Description
```http
POST /api/ai/generate-product-description
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "name": "Eco-Friendly T-Shirt",
  "category": "Clothing",
  "keyFeatures": "100% organic cotton, fair trade, biodegradable packaging",
  "additionalContext": "Target environmentally conscious consumers"
}
```

#### Health Check
```http
GET /api/ai/health
```

### 2. Campaign-Specific Endpoints

#### Generate Description for Existing Campaign
```http
POST /api/campagnes/{campaignId}/generate-description?targetAudience=Young professionals&additionalContext=Focus on innovation
Authorization: Bearer <jwt-token>
```

#### Preview Campaign Description (without saving)
```http
POST /api/campagnes/preview-description?campaignName=Tech Innovation&category=Technology&targetAudience=Tech enthusiasts
Authorization: Bearer <jwt-token>
```

#### Generate Complete Description (Campaign + Product)
```http
POST /api/campagnes/{campaignId}/generate-complete-description?targetAudience=Tech enthusiasts&keyFeatures=AI-powered, user-friendly&additionalContext=Emphasize cutting-edge technology
Authorization: Bearer <jwt-token>
```

### 3. Product-Specific Endpoints

#### Generate Description for Existing Product
```http
POST /api/produits/{productId}/generate-description?keyFeatures=Wireless, long battery life&additionalContext=Premium quality
Authorization: Bearer <jwt-token>
```

#### Preview Product Description (without saving)
```http
POST /api/produits/preview-description?productName=Smart Watch&category=Electronics&keyFeatures=Health tracking, GPS&additionalContext=For fitness enthusiasts
Authorization: Bearer <jwt-token>
```

## Response Format

### Success Response
```json
{
  "success": true,
  "generatedText": "Your AI-generated description here..."
}
```

### Error Response
```json
{
  "success": false,
  "errorMessage": "Error description here"
}
```

### Campaign/Product Update Response
```json
{
  "success": true,
  "message": "Description generated successfully",
  "campaign": {
    "id": 1,
    "nom": "Campaign Name",
    "description": "AI-generated description...",
    // ... other campaign fields
  }
}
```

## Frontend Integration Examples

### JavaScript/TypeScript Example
```typescript
// Generate campaign description
async function generateCampaignDescription(campaignId: number, targetAudience?: string) {
  const response = await fetch(`/api/campagnes/${campaignId}/generate-description`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: new URLSearchParams({
      targetAudience: targetAudience || '',
      additionalContext: 'Generate engaging content'
    })
  });
  
  const result = await response.json();
  if (result.success) {
    console.log('Generated description:', result.campaign.description);
  } else {
    console.error('Error:', result.message);
  }
}

// Preview description before saving
async function previewDescription(name: string, category: string) {
  const response = await fetch('/api/ai/generate-campaign-description', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      name,
      category,
      targetAudience: 'General audience'
    })
  });
  
  const result = await response.json();
  return result.generatedText;
}
```

### Angular Service Example
```typescript
@Injectable()
export class AIService {
  constructor(private http: HttpClient) {}
  
  generateCampaignDescription(request: AIGenerationRequest): Observable<AIGenerationResponse> {
    return this.http.post<AIGenerationResponse>('/api/ai/generate-campaign-description', request);
  }
  
  generateProductDescription(request: AIGenerationRequest): Observable<AIGenerationResponse> {
    return this.http.post<AIGenerationResponse>('/api/ai/generate-product-description', request);
  }
  
  updateCampaignWithAI(campaignId: number, params: any): Observable<any> {
    const queryParams = new HttpParams({ fromObject: params });
    return this.http.post(`/api/campagnes/${campaignId}/generate-description`, null, { params: queryParams });
  }
}
```

## Security

- All AI endpoints require authentication (JWT token)
- Only users with `ADMIN` or `ANNONCEUR` roles can access AI generation endpoints
- Campaign owners can only generate descriptions for their own campaigns

## Error Handling

Common error scenarios:
1. **Invalid API Key**: Check your Gemini API key configuration
2. **Rate Limiting**: Gemini API has rate limits, implement retry logic
3. **Network Issues**: Handle timeout and connection errors
4. **Authorization**: Ensure proper JWT token and user roles

## Best Practices

1. **Caching**: The service doesn't implement caching by default, consider adding it for frequently requested descriptions
2. **Rate Limiting**: Implement client-side rate limiting to avoid hitting API limits
3. **Fallback**: Always have fallback content in case AI generation fails
4. **Validation**: Validate generated content before saving to database
5. **User Experience**: Show loading states during AI generation (can take 2-10 seconds)

## Troubleshooting

### Common Issues

1. **"AI service temporarily unavailable"**
   - Check your internet connection
   - Verify Gemini API key is correct
   - Check Gemini API status

2. **"Insufficient permissions"**
   - Ensure user has ADMIN or ANNONCEUR role
   - Check JWT token validity

3. **"Campaign/Product not found"**
   - Verify the ID exists in database
   - Check user has access to the resource

### Logs
Check application logs for detailed error messages:
```bash
tail -f logs/application.log | grep "AI\|Gemini"
```