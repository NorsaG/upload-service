package edu.project.upload.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.domain.Sort;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TagsListRequest extends BaseRequest {

}