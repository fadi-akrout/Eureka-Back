package org.example.eurekaback.Service.EmailService;

/**
 * Interface for sending email notifications in the application
 */
public interface EmailService {
    
    /**
     * Sends a campaign invitation email to a panelist
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param panelisteNom panelist name
     * @param campagneNom campaign name
     * @param campagneDescription campaign description
     * @param acceptUrl URL for accepting the invitation
     * @param rejectUrl URL for rejecting the invitation
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendCampagneInvitationEmail(String to, String subject, String panelisteNom, 
                                        String campagneNom, String campagneDescription,
                                        String acceptUrl, String rejectUrl);
    
    /**
     * Sends a simple text email
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param text email content
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendSimpleEmail(String to, String subject, String text);
}