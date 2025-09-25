package edu.project.upload.requests;

import edu.project.upload.model.Visibility;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileChangeVisibilityRequest extends BaseRequest {
    private Visibility visibility;
}
