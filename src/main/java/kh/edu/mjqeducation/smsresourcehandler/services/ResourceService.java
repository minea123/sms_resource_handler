package kh.edu.mjqeducation.smsresourcehandler.services;

import org.springframework.core.io.Resource;

public interface ResourceService {
    String getRealResourcePath(String resourceUrl);
    Resource getFileSystemAsResource(String filePath);
    Resource getFileFromOtherServer(String realFilePath);
}
