package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Admin;
import org.example.eurekaback.Entity.Reponse;
import org.example.eurekaback.Service.ServiceAdmin.AdminService;
import org.example.eurekaback.Service.ServiceReponse.ReponseService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/api/reponses")
@AllArgsConstructor
public class ReponseController {

    private final ReponseService reponseService;

    @GetMapping
    public ResponseEntity<List<Reponse>> getAllReponses() {
        return ResponseEntity.ok(reponseService.getAllReponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reponse> getReponseById(@PathVariable int id) {
        return reponseService.getReponseById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Reponse not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Reponse> createReponse(@Valid @RequestBody Reponse reponse) {
        return new ResponseEntity<>(reponseService.createReponse(reponse), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReponse(@PathVariable int id) {
        reponseService.deleteReponse(id);
        return ResponseEntity.noContent().build();
    }
}