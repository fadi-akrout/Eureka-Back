package org.example.eurekaback.Controller;

import org.example.eurekaback.Service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Serve a file by its name
     * 
     * @param fileName The name of the file to serve
     * @return The file as a resource
     * @throws IOException If an error occurs while serving the file
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) throws IOException {
        try {
            // Normalize the file name to handle potential double slashes or other URL encoding issues
            fileName = fileName.replaceAll("//+", "/");
            System.out.println("FileController: Serving file: " + fileName);
            
            Path filePath = fileStorageService.getFilePath(fileName);
            System.out.println("FileController: File path: " + filePath);
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(fileName);
                System.out.println("FileController: File exists, serving with content type: " + contentType);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.out.println("FileController: File not found: " + fileName + " at path: " + filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            System.out.println("FileController: Malformed URL for file: " + fileName + ", Error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("FileController: Error serving file: " + fileName + ", Error: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Determine the content type of a file based on its extension
     * 
     * @param fileName The name of the file
     * @return The content type
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