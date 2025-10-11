package org.example.eurekaback.Service.EmailService;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService that logs emails instead of sending them
 * In a production environment, this would be replaced with an actual email sending implementation
 */
@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    @Override
    public boolean sendCampagneInvitationEmail(String to, String subject, String panelisteNom, 
                                               String campagneNom, String campagneDescription,
                                               String acceptUrl, String rejectUrl) {
        // Log the email details instead of actually sending it
        logger.info("Sending campaign invitation email:");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("Panelist Name: {}", panelisteNom);
        logger.info("Campaign: {}", campagneNom);
        logger.info("Description: {}", campagneDescription);
        logger.info("Accept URL: {}", acceptUrl);
        logger.info("Reject URL: {}", rejectUrl);
        
        // In a real implementation, this would use JavaMailSender to send the actual email
        // For now, we just log it and return success
        return true;
    }
    
    @Override
    public boolean sendSimpleEmail(String to, String subject, String text) {
        // Log the email details instead of actually sending it
        logger.info("Sending simple email:");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("Content: {}", text);
        
        // In a real implementation, this would use JavaMailSender to send the actual email
        // For now, we just log it and return success
        return true;
    }
}