package edu.project.upload.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileRenameRequest extends BaseRequest {
    private String newFileName;
}
