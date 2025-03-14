package kh.edu.mjqeducation.smsresourcehandler.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResourceServiceImpl implements ResourceService {
    @Value("${sms.storage.path}")
    private String storagePath;

    @Value("sms.servers")
    private String servers;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String getRealResourcePath(String resourceUrl) {
        return String.join("/", storagePath, resourceUrl);
    }

    @Override
    public Resource getFileSystemAsResource(String filePath) {
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return null;
        }

        return resource;
    }

    @Override
    public Resource getFileFromOtherServer(String realFilePath) {
        String[] serverList = servers.split(",");

        for (String server : serverList) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "SpringBootApp/1.0"); // Optional custom header
            headers.set("Accept", MediaType.APPLICATION_OCTET_STREAM_VALUE); // Expect binary response

            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("path", realFilePath);

            // Create the request entity
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestMap, headers);
            String endpoint = "https://" + server + "/api/resource";

             Resource resource = restTemplate.postForObject(endpoint , request, Resource.class);

             if (resource != null) {
                 return resource;
             }
        }

        return null;
    }
}
