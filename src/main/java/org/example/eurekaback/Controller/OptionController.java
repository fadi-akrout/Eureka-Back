package org.example.eurekaback.Controller;
import lombok.AllArgsConstructor;

import org.example.eurekaback.Entity.Option;
import org.example.eurekaback.Service.ServiceOption.OptionService;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/options")
@AllArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @GetMapping
    public ResponseEntity<List<Option>> getAllOptions() {
        return ResponseEntity.ok(optionService.getAllOptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Option> getOptionById(@PathVariable int id) {
        return optionService.getOptionById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found with id: " + id));
    }

    @PostMapping
    public ResponseEntity<Option> createOption(@Valid @RequestBody Option option) {
        return new ResponseEntity<>(optionService.createOption(option), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOption(@PathVariable int id) {
        optionService.deleteOption(id);
        return ResponseEntity.noContent().build();
    }
}