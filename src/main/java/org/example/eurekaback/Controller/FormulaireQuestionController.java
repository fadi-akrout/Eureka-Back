package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Formulaire;
import org.example.eurekaback.Entity.FormulaireQuestion;
import org.example.eurekaback.Repository.FormulaireQuestionRepository;
import org.example.eurekaback.Service.ServiceFormulaire.FormulaireService;
import org.example.eurekaback.Service.ServiceFormulaireQuestion.FormulaireQuestionService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/formulaire-questions")
@AllArgsConstructor
public class FormulaireQuestionController {

    private final FormulaireQuestionService formulaireQuestionService;
    private final FormulaireService formulaireService;
    private final FormulaireQuestionRepository formulaireQuestionRepository;

    @GetMapping
    public ResponseEntity<List<FormulaireQuestion>> getAllFormulaireQuestions() {
        return ResponseEntity.ok(formulaireQuestionService.getAllFormulaireQuestions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormulaireQuestion> getFormulaireQuestionById(@PathVariable int id) {
        return formulaireQuestionService.getFormulaireQuestionById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("FormulaireQuestion not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<FormulaireQuestion> createFormulaireQuestion(@Valid @RequestBody FormulaireQuestion formulaireQuestion) {
        return new ResponseEntity<>(formulaireQuestionService.createFormulaireQuestion(formulaireQuestion), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFormulaireQuestion(@PathVariable int id) {
        formulaireQuestionService.deleteFormulaireQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/formulaire/{formulaireId}")
    public ResponseEntity<List<FormulaireQuestion>> getFormulaireQuestionsByFormulaire(@PathVariable int formulaireId) {
        Formulaire formulaire = formulaireService.getFormulaireById(formulaireId)
                .orElseThrow(() -> new ResourceNotFoundException("Formulaire not found with id: " + formulaireId));

        return ResponseEntity.ok( formulaireQuestionRepository.findByFormulaire(formulaire));
    }
}