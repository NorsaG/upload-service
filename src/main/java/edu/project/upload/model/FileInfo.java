package edu.project.upload.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@Document(collection = "files")
public class FileInfo {
    @Id
    private String fileId;
    private String userId;
    private String fileName;
    private Set<String> tags;
    private long fileSize;
    private Visibility visibility;
    private LocalDateTime uploadDate;
    private String hash;
    private String contentType;
    private String downloadLink;
    private String gridFsId;
}
