package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Feedback;
import org.example.eurekaback.Service.ServiceFeedback.FeedbackService;
import org.hibernate.query.Page;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/api/feedbacks")
@AllArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable int id) {
        return feedbackService.getFeedbackById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@Valid @RequestBody Feedback feedback) {
        return new ResponseEntity<>(feedbackService.createFeedback(feedback), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable int id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/campagne/{campagneId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByCampagne(@PathVariable int campagneId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByCampagne(campagneId));
    }

    @GetMapping("/campagne/{campagneId}/note-moyenne")
    public ResponseEntity<Double> getNoteMoyennePourCampagne(@PathVariable int campagneId) {
        return ResponseEntity.ok(feedbackService.getNoteMoyennePourCampagne(campagneId));
    }

    /*@GetMapping("/campagne/{campagneId}/paginated")
    public ResponseEntity<Page<Feedback>> getFeedbacksByCampagnePaginated(
            @PathVariable int campagneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByCampagnePaginated(campagneId, page, size));
    }*/
}