package kh.edu.mjqeducation.smsresourcehandler.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import kh.edu.mjqeducation.smsresourcehandler.dto.FileContext;
import kh.edu.mjqeducation.smsresourcehandler.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.apache.catalina.connector.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.nio.file.FileStore;
import org.apache.commons.io.input.TeeInputStream;

@Controller
public class ResourceController {
    private static final Logger LOGGER = LogManager.getLogger(ResourceController.class);

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
        MediaType mediaType = resourceService.getResourceMediaType(realResourcePath);
        LOGGER.debug("Content Type: {}", mediaType.toString());

        Resource requestResource = resourceService.getFileSystemAsResource(realResourcePath);

        // resource exiss
        if (requestResource != null && requestResource.exists()) {
            // Return the file as a ResponseEntity
            return ResponseEntity.ok()
                .contentType(mediaType)
                .body(requestResource);
        }

        LOGGER.warn("Resource not found, getting from other server: {}", realResourcePath);

        Resource downloadedResource = resourceService.getFileFromOtherServer(realResourcePath);

        // resource not found on other server
        if (downloadedResource == null) {
            LOGGER.warn("Can't find resource on another servers, something is wrong :)");
            return ResponseEntity.notFound().build();
        }

        // save to local
        resourceService.writeToDisk(downloadedResource.getContentAsByteArray(), realResourcePath);

        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(downloadedResource);
    }

    @PostMapping("/api/download")
    public ResponseEntity<Resource> download(@RequestBody FileContext fileContext) throws IOException {
        return ResponseEntity
            .ok()
            .contentType(resourceService.getResourceMediaType(fileContext.getPath()))
            .body(resourceService.getFileSystemAsResource(fileContext.getPath()));
    }

    @PostMapping(value = "/api/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(@RequestPart("file") MultipartFile file, @RequestPart("destination") String destination) throws IOException { 
        resourceService.writeToDisk(file.getBytes(), destination);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
