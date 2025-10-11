package org.example.eurekaback.Controller;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.eurekaback.Entity.Formulaire;
import org.example.eurekaback.Entity.FormulaireQuestion;
import org.example.eurekaback.Entity.Question;
import org.example.eurekaback.Service.ServiceFormulaire.FormulaireService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/formulaires")
@AllArgsConstructor
@Slf4j
public class FormulaireController {

    private final FormulaireService formulaireService;

    @GetMapping
    public ResponseEntity<List<Formulaire>> getAllFormulaires() {
        return ResponseEntity.ok(formulaireService.getAllFormulaires());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Formulaire> getFormulaireById(@PathVariable int id) {
        return formulaireService.getFormulaireById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Formulaire not found with id: " + id));
    }

    @GetMapping("/{id}/with-questions")
    public ResponseEntity<Formulaire> getFormulaireWithQuestions(@PathVariable int id) {
        log.info("Received request to get formulaire with questions for id: {}", id);
        try {
            return formulaireService.getFormulaireWithQuestions(id)
                    .map(formulaire -> {
                        log.info("Found formulaire with id: {}, questions count: {}", 
                                formulaire.getId(), 
                                formulaire.getFormulaireQuestions() != null ? formulaire.getFormulaireQuestions().size() : 0);
                        return ResponseEntity.ok(formulaire);
                    })
                    .orElseThrow(() -> {
                        log.error("Formulaire not found with id: {}", id);
                        return new ResourceNotFoundException("Formulaire not found with id: " + id);
                    });
        } catch (Exception e) {
            log.error("Error retrieving formulaire with questions for id: {}", id, e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<Formulaire> createFormulaire(@Valid @RequestBody Formulaire formulaire) {
        return new ResponseEntity<>(formulaireService.createFormulaire(formulaire), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFormulaire(@PathVariable int id) {
        formulaireService.deleteFormulaire(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/campagne/{campagneId}")
    public ResponseEntity<Formulaire> getFormulaireByCampagne(@PathVariable int campagneId) {
        log.info("Received request to get formulaire for campagne id: {}", campagneId);
        try {
            return formulaireService.getFormulaireByCampagne(campagneId)
                    .map(formulaire -> {
                        log.info("Found formulaire with id: {} for campagne: {}, questions count: {}", 
                                formulaire.getId(), 
                                campagneId,
                                formulaire.getFormulaireQuestions() != null ? formulaire.getFormulaireQuestions().size() : 0);
                        return ResponseEntity.ok(formulaire);
                    })
                    .orElseThrow(() -> {
                        log.error("Formulaire not found for campagne with id: {}", campagneId);
                        return new ResourceNotFoundException("Formulaire not found for campagne with id: " + campagneId);
                    });
        } catch (Exception e) {
            log.error("Error retrieving formulaire for campagne id: {}", campagneId, e);
            throw e;
        }
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<List<FormulaireQuestion>> getFormulaireQuestions(@PathVariable int id) {
        List<FormulaireQuestion> questions = formulaireService.getQuestionsForForm(id);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/with-questions")
    public ResponseEntity<Formulaire> createFormulaireWithQuestions(
            @Valid @RequestBody FormulaireWithQuestionsRequest request) {
        return new ResponseEntity<>(
                formulaireService.createFormulaireWithQuestions(request.getFormulaire(), request.getQuestions()),
                HttpStatus.CREATED);
    }
}

@Data
class FormulaireWithQuestionsRequest {
    @NotNull
    private Formulaire formulaire;
    @NotNull
    private List<Question> questions;
}