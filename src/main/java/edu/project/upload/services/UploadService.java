package edu.project.upload.services;

import edu.project.upload.model.FileInfo;
import edu.project.upload.requests.FileUploadRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import edu.project.upload.model.Visibility;

import java.util.Set;

public interface UploadService {
    FileInfo uploadFile(FileUploadRequest request);

    FileInfo renameFile(String fileId, String newFileName, String userId);

    Page<FileInfo> listFiles(String userId, Visibility visibility, String tag, PageRequest pageRequest);

    void deleteFile(String fileId, String userId);

    ResponseEntity<?> downloadFile(String fileId);

    FileInfo changeVisibility(String fileId, String userId, Visibility visibility);

      Set<String> listTags(String userId );
}
