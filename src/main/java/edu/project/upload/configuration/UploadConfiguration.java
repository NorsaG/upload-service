package edu.project.upload.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "upload")
@Data
@Configuration
public class UploadConfiguration {
    private int maxThreads;
    private String hashAlgo;
}
