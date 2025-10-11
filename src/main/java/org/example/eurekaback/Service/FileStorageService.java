package org.example.eurekaback.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Store a file and return the path where it was stored
     * 
     * @param file The file to store
     * @return The path where the file was stored
     * @throws IOException If an error occurs during file storage
     */
    public String storeFile(MultipartFile file) throws IOException {
        // Create the upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate a unique file name to prevent conflicts
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Copy the file to the upload directory
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    /**
     * Get the full URL for a stored file
     * 
     * @param fileName The name of the file
     * @return The full URL for the file
     */
    public String getFileUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        // Ensure we don't have double slashes by normalizing the path
        String normalizedPath = "/api/files/" + fileName;
        // Replace any potential double slashes with single slashes
        normalizedPath = normalizedPath.replaceAll("//+", "/");
        
        // Log the generated URL for debugging
        System.out.println("FileStorageService: Generated file URL: " + normalizedPath);
        
        return normalizedPath;
    }

    /**
     * Get the path to a stored file
     * 
     * @param fileName The name of the file
     * @return The path to the file
     */
    public Path getFilePath(String fileName) {
        System.out.println("FileStorageService: Getting file path for: " + fileName);
        
        // Normalize the file name to handle potential double slashes or other URL encoding issues
        if (fileName != null) {
            fileName = fileName.replaceAll("//+", "/");
            // Remove any leading slash that might be present
            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }
            
            // If the file name contains "api/files/" (from a URL), extract just the file name
            if (fileName.contains("api/files/")) {
                fileName = fileName.substring(fileName.lastIndexOf("api/files/") + "api/files/".length());
                System.out.println("FileStorageService: Extracted file name from URL: " + fileName);
            }
        }
        
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        System.out.println("FileStorageService: Resolved file path: " + filePath);
        
        return filePath;
    }
}