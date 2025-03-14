package kh.edu.mjqeducation.smsresourcehandler.controller;

import java.io.IOException;

import kh.edu.mjqeducation.smsresourcehandler.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileStore;

@Controller
public class ResourceController {
    private static final Logger LOGGER = LogManager.getLogger(ResourceController.class);

    @Value("${sms.storage.path}")
    private String storagePath;

    @Value("${sms.servers}")
    private String targetServers;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ResourceService resourceService;

    @GetMapping("/serve/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) throws IOException {
        String resourceUrl = request.getRequestURI().substring("/serve/".length());
        String realResourcePath = resourceService.getRealResourcePath(resourceUrl);

        LOGGER.debug("Resource URL: {}", resourceUrl);
        LOGGER.debug("Real Resource URL: {}", realResourcePath);

        Resource requestResource = resourceService.getFileSystemAsResource(realResourcePath);

        if (requestResource.exists()) {
            // Return the file as a ResponseEntity
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG) // Set MIME type
                .body(requestResource);
        }

        LOGGER.warn("Resource not found, getting from other server: {}", realResourcePath);
        Resource resource = resourceService.getFileFromOtherServer(realResourcePath);

        return ResponseEntity.notFound().build();
    }
}
