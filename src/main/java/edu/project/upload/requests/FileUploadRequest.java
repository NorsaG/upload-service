package edu.project.upload.requests;

import edu.project.upload.model.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class FileUploadRequest extends BaseRequest {
    private String userId;
    private String fileName;
    private Visibility visibility;
    private List<String> tags;
    private String contentType;
    private MultipartFile file;

}
