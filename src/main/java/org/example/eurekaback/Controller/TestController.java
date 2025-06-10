package org.example.eurekaback.Controller;

import org.example.eurekaback.Dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/public")
public class TestController {

    @GetMapping("/test")
    public ResponseEntity<?> testPublicEndpoint() {
        return ResponseEntity.ok(new MessageResponse("Public endpoint is working!"));
    }
}