package org.example.eurekaback.Controller;

import org.example.eurekaback.Service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

/**
 * Controller to handle image requests at /api/images path
 * This controller serves as a proxy to the actual file location
 */
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final FileStorageService fileStorageService;

    public ImageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Handle requests to /api/images/api/files/{fileName}
     * This pattern appears in the logs and needs to be supported
     */
    @GetMapping("/api/files/{fileName:.+}")
    public ResponseEntity<Resource> serveImageFromNestedPath(@PathVariable String fileName) throws IOException {
        return serveImageFile(fileName);
    }

    /**
     * Handle requests to /api/images/{fileName}
     * This is the standard pattern that should be used
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String fileName) throws IOException {
        return serveImageFile(fileName);
    }

    /**
     * Common method to serve image files
     */
    private ResponseEntity<Resource> serveImageFile(String fileName) throws IOException {
        try {
            // Normalize the file name to handle potential double slashes or other URL encoding issues
            fileName = fileName.replaceAll("//+", "/");
            
            Path filePath = fileStorageService.getFilePath(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(fileName);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.out.println("Image not found: " + fileName + " at path: " + filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL for image: " + fileName + ", Error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Error serving image: " + fileName + ", Error: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Determine the content type of a file based on its extension
     */
    private String determineContentType(String fileName) {
        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.toLowerCase().endsWith(".bmp")) {
            return "image/bmp";
        } else if (fileName.toLowerCase().endsWith(".webp")) {
            return "image/webp";
        } else {
            return "application/octet-stream";
        }
    }
}