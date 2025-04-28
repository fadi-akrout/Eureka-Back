package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;

import org.example.eurekaback.Entity.ResultatAnalyse;
import org.example.eurekaback.Service.ServiceResultatAnalyse.ResultatAnalyseService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/api/resultats-analyse")
@AllArgsConstructor
public class ResultatAnalyseController {

    private final ResultatAnalyseService resultatAnalyseService;

    @GetMapping
    public ResponseEntity<List<ResultatAnalyse>> getAllResultatsAnalyse() {
        return ResponseEntity.ok(resultatAnalyseService.getAllResultatsAnalyse());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultatAnalyse> getResultatAnalyseById(@PathVariable int id) {
        return resultatAnalyseService.getResultatAnalyseById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("ResultatAnalyse not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<ResultatAnalyse> createResultatAnalyse(@Valid @RequestBody ResultatAnalyse resultatAnalyse) {
        return new ResponseEntity<>(resultatAnalyseService.createResultatAnalyse(resultatAnalyse), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResultatAnalyse(@PathVariable int id) {
        resultatAnalyseService.deleteResultatAnalyse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generer/{campagneId}")
    public ResponseEntity<ResultatAnalyse> genererAnalysePourCampagne(@PathVariable int campagneId) {
        return ResponseEntity.ok(resultatAnalyseService.genererAnalysePourCampagne(campagneId));
    }
}