package edu.project.upload.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Data
@RequiredArgsConstructor
public class CommonApplicationConfiguration {
    private final UploadConfiguration uploadConfiguration;

    @Bean
    public ExecutorService executorService(){
        return Executors.newFixedThreadPool(uploadConfiguration.getMaxThreads());
    }

    @Bean
    @SneakyThrows
    public MessageDigest messageDigest(){
        return MessageDigest.getInstance(uploadConfiguration.getHashAlgo());
    }
}
