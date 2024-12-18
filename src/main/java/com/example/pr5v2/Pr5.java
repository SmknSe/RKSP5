package com.example.pr5v2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.util.StringUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootApplication
@RestController
public class Pr5 {
    //Семейкин Сергей ИКБО-16-21 (SmknSe)
	@Value("${file.storage.location}")
    private String fileStorageLocation;

    @Value("${server.port}")
    private String serverPort;

    public static void main(String[] args) {
        SpringApplication.run(Pr5.class, args);
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            Path targetLocation = Paths.get(fileStorageLocation).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "File uploaded successfully: " + fileName;
        } catch (IOException ex) {
            return "Could not upload file: " + fileName;
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(fileStorageLocation).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                System.out.println("File " + fileName + " was downloaded");

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new FileNotFoundException("Downloading file not found");
            }
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/files")
    public List<String> listFiles() throws IOException {
        return Files.list(Paths.get(fileStorageLocation))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    @GetMapping("/status")
    public String getStatus(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        return "Node is active on port " + serverPort + ". Client IP: " + clientIp;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }


}
