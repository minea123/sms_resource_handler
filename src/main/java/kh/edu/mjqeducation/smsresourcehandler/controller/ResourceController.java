package kh.edu.mjqeducation.smsresourcehandler.controller;

import java.io.IOException;
import kh.edu.mjqeducation.smsresourcehandler.dto.FileContext;
import kh.edu.mjqeducation.smsresourcehandler.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ResourceController {
    private static final Logger LOGGER = LogManager.getLogger(ResourceController.class);
    private static final Resource notFoundImage = new FileSystemResource("/App/aii_school_prod/public/assets/images/no-images/no-image.png");

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

        // resource exists
        if (requestResource != null && requestResource.exists()) {
            LOGGER.debug("Resource found locally {}", realResourcePath);
            // Return the file as a ResponseEntity
            return ResponseEntity.ok()
                .contentType(mediaType)
                .body(requestResource);
        }

        LOGGER.debug("Resource not found, getting from other server: {}", realResourcePath);

        Resource downloadedResource = resourceService.getFileFromOtherServer(realResourcePath);

        // resource not found on other server
        if (downloadedResource == null) {
            LOGGER.warn("Can't find resource on another servers, something is wrong :)");
            return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(notFoundImage);
        }

        // save to local
        resourceService.writeToDisk(downloadedResource, realResourcePath);

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
        // Convert file multipart to Resource
        Resource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }

            @Override
            public long contentLength() {
                return file.getSize();
            }
        };

        resourceService.writeToDisk(resource, destination);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
