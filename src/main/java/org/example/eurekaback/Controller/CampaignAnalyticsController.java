package org.example.eurekaback.Controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Dto.CampaignAnalyticsRequestDTO;
import org.example.eurekaback.Dto.CampaignAnalyticsResponseDTO;
import org.example.eurekaback.Service.CampaignAnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/campaign-analytics")
@AllArgsConstructor
@Slf4j
public class CampaignAnalyticsController {

    private final CampaignAnalyticsService campaignAnalyticsService;

    /**
     * Generate comprehensive analytics for a campaign
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<CampaignAnalyticsResponseDTO> generateCampaignAnalytics(
            @RequestBody CampaignAnalyticsRequestDTO request) {
        
        try {
            log.info("Received request to generate analytics for campaign ID: {}", request.getCampaignId());
            
            if (request.getCampaignId() == null) {
                CampaignAnalyticsResponseDTO errorResponse = new CampaignAnalyticsResponseDTO("Campaign ID is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            CampaignAnalyticsResponseDTO response = campaignAnalyticsService.generateCampaignAnalytics(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in generateCampaignAnalytics endpoint", e);
            CampaignAnalyticsResponseDTO errorResponse = new CampaignAnalyticsResponseDTO("Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Generate analytics for a campaign with GET request (simplified)
     */
    @GetMapping("/{campaignId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<CampaignAnalyticsResponseDTO> getCampaignAnalytics(
            @PathVariable Integer campaignId,
            @RequestParam(defaultValue = "true") boolean includeKeywords,
            @RequestParam(defaultValue = "true") boolean includeDailyTrends,
            @RequestParam(defaultValue = "false") boolean forceRefresh,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "20") Integer maxKeywords) {
        
        try {
            log.info("Received GET request for analytics for campaign ID: {}", campaignId);
            
            CampaignAnalyticsRequestDTO request = new CampaignAnalyticsRequestDTO();
            request.setCampaignId(campaignId);
            request.setIncludeKeywords(includeKeywords);
            request.setIncludeDailyTrends(includeDailyTrends);
            request.setForceRefresh(forceRefresh);
            request.setMaxKeywords(maxKeywords);
            
            // Parse dates if provided
            if (startDate != null && !startDate.isEmpty()) {
                try {
                    request.setStartDate(LocalDate.parse(startDate));
                } catch (Exception e) {
                    log.warn("Invalid start date format: {}", startDate);
                }
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                try {
                    request.setEndDate(LocalDate.parse(endDate));
                } catch (Exception e) {
                    log.warn("Invalid end date format: {}", endDate);
                }
            }
            
            CampaignAnalyticsResponseDTO response = campaignAnalyticsService.generateCampaignAnalytics(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in getCampaignAnalytics endpoint", e);
            CampaignAnalyticsResponseDTO errorResponse = new CampaignAnalyticsResponseDTO("Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Generate analytics with minimal data (quick overview)
     */
    @GetMapping("/{campaignId}/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANNONCEUR')")
    public ResponseEntity<CampaignAnalyticsResponseDTO> getCampaignAnalyticsOverview(
            @PathVariable Integer campaignId) {
        
        try {
            log.info("Received request for analytics overview for campaign ID: {}", campaignId);
            
            CampaignAnalyticsRequestDTO request = new CampaignAnalyticsRequestDTO();
            request.setCampaignId(campaignId);
            request.setIncludeKeywords(false);
            request.setIncludeDailyTrends(false);
            request.setForceRefresh(false);
            
            CampaignAnalyticsResponseDTO response = campaignAnalyticsService.generateCampaignAnalytics(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in getCampaignAnalyticsOverview endpoint", e);
            CampaignAnalyticsResponseDTO errorResponse = new CampaignAnalyticsResponseDTO("Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint for analytics service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Campaign Analytics Service is running");
    }
}