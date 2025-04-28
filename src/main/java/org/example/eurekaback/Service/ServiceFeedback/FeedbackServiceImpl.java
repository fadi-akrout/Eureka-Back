package org.example.eurekaback.Service.ServiceFeedback;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Feedback;
import org.example.eurekaback.Entity.Produit;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.FeedbackRepository;
import org.example.eurekaback.Repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final CampagneRepository campagneRepository;
    private final ProduitRepository produitRepository;

    @Override
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Override
    public Optional<Feedback> getFeedbackById(int id) {
        return feedbackRepository.findById(id);
    }

    @Override
    public Feedback createFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    @Override
    public void deleteFeedback(int id) {
        feedbackRepository.deleteById(id);
    }
    @Override
    public List<Feedback> getFeedbacksByCampagne(int campagneId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne not found with id: " + campagneId));

        return campagne.getFeedbacks();
    }

    @Override
    public double getNoteMoyennePourCampagne(int campagneId) {
        List<Feedback> feedbacks = getFeedbacksByCampagne(campagneId);

        if (feedbacks.isEmpty()) {
            return 0.0;
        }

        double sum = feedbacks.stream()
                .mapToInt(Feedback::getNote)
                .sum();

        return sum / feedbacks.size();
    }
   /* private void updateProductAverageRating(int campagneId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne not found with id: " + campagneId));

        Optional<Produit> produitOpt = produitRepository.findByCampagne(campagne);
        if (produitOpt.isPresent()) {
            Produit produit = produitOpt.get();
            double averageRating = getNoteMoyennePourCampagne(campagneId);
            produit.setNoteMoyenne((float) averageRating);
            produitRepository.save(produit);
        }
    }*/
}