package kh.edu.mjqeducation.smsresourcehandler.services;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public interface ResourceService {
    String getRealResourcePath(String resourceUrl);
    Resource getFileSystemAsResource(String filePath);
    Resource getFileFromOtherServer(String realFilePath);
    MediaType getResourceMediaType(String path);
    void writeToDisk(Resource file, String destination) throws IOException;
}
