package edu.project.upload.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.domain.Sort;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileListRequest extends BaseRequest {

    private String visibility;
    private String tag;

    private String sortBy = "fileName";
    private Sort.Direction sortDirection = Sort.Direction.ASC;

}