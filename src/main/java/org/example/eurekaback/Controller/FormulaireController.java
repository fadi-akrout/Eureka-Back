package org.example.eurekaback.Controller;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.eurekaback.Entity.Formulaire;
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
        return formulaireService.getFormulaireByCampagne(campagneId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Formulaire not found for campagne with id: " + campagneId));
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