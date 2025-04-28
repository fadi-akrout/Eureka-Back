package org.example.eurekaback.Service.ServiceFeedback;

import org.example.eurekaback.Entity.Feedback;
import org.hibernate.query.Page;

import java.util.List;
import java.util.Optional;

public interface FeedbackService {
    List<Feedback> getAllFeedbacks();
    Optional<Feedback> getFeedbackById(int id);
    Feedback createFeedback(Feedback feedback);
    void deleteFeedback(int id);

    List<Feedback> getFeedbacksByCampagne(int campagneId);
    double getNoteMoyennePourCampagne(int campagneId);


}
