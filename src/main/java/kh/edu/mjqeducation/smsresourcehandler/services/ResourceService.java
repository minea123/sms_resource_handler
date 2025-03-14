package kh.edu.mjqeducation.smsresourcehandler.services;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceService {
    String getRealResourcePath(String resourceUrl);
    Resource getFileSystemAsResource(String filePath);
    Resource getFileFromOtherServer(String realFilePath);
    MediaType getResourceMediaType(String path);
    void writeToDisk(byte[] byteArray, String destination) throws IOException;
}
