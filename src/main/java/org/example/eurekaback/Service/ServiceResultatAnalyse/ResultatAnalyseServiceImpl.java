package org.example.eurekaback.Service.ServiceResultatAnalyse;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Campagne;
import org.example.eurekaback.Entity.Feedback;
import org.example.eurekaback.Entity.ResultatAnalyse;
import org.example.eurekaback.Repository.CampagneRepository;
import org.example.eurekaback.Repository.ResultatAnalyseRepository;
import org.example.eurekaback.Service.ServiceFeedback.FeedbackService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ResultatAnalyseServiceImpl implements ResultatAnalyseService {
    private final ResultatAnalyseRepository resultatAnalyseRepository;
    private final CampagneRepository campagneRepository;
    private final FeedbackService feedbackService;

    @Override
    public List<ResultatAnalyse> getAllResultatsAnalyse() {
        return resultatAnalyseRepository.findAll();
    }

    @Override
    public Optional<ResultatAnalyse> getResultatAnalyseById(int id) {
        return resultatAnalyseRepository.findById(id);
    }

    @Override
    public ResultatAnalyse createResultatAnalyse(ResultatAnalyse resultatAnalyse) {
        return resultatAnalyseRepository.save(resultatAnalyse);
    }

    @Override
    public void deleteResultatAnalyse(int id) {
        resultatAnalyseRepository.deleteById(id);
    }
    @Override
    @Transactional
    public ResultatAnalyse genererAnalysePourCampagne(int campagneId) {
        Campagne campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new ResourceNotFoundException("Campagne not found with id: " + campagneId));

        ResultatAnalyse resultatAnalyse = campagne.getResultatAnalyse();
        if (resultatAnalyse == null) {
            // Create new ResultatAnalyse if it doesn't exist
            resultatAnalyse = new ResultatAnalyse();
            resultatAnalyse = resultatAnalyseRepository.save(resultatAnalyse);

            // Assign it to the campaign
            campagne.setResultatAnalyse(resultatAnalyse);
            campagneRepository.save(campagne);
        }

        // Calculate average rating
        double noteMoyenne = feedbackService.getNoteMoyennePourCampagne(campagneId);

        // Count feedback by rating
        List<Feedback> feedbacks = feedbackService.getFeedbacksByCampagne(campagneId);
        Map<Integer, Long> countByRating = feedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::getNote, Collectors.counting()));

        // Calculate percentages
        Map<String, Float> resultats = new HashMap<>();
        resultats.put("note_moyenne", (float) noteMoyenne);

        int totalFeedbacks = feedbacks.size();
        if (totalFeedbacks > 0) {
            for (int i = 0; i <= 5; i++) {
                long count = countByRating.getOrDefault(i, 0L);
                float percentage = (float) count / totalFeedbacks * 100;
                resultats.put("pourcentage_note_" + i, percentage);
            }
        }

        // Add participation rate
        int totalPanelistes = campagne.getPanelisteCampagnes().size();
        int panelistesWithFeedback = (int) feedbacks.stream()
                .map(f -> f.getPaneliste().getId())
                .distinct()
                .count();

        if (totalPanelistes > 0) {
            float participationRate = (float) panelistesWithFeedback / totalPanelistes * 100;
            resultats.put("taux_participation", participationRate);
        }

        // Save results
        resultatAnalyse.setResultats(resultats);
        return resultatAnalyseRepository.save(resultatAnalyse);
    }

}