package edu.project.upload.requests;

import lombok.Data;

@Data
public abstract class BaseRequest {
    private String userId;
    private int page;
    private int size = 10;
}
