package edu.project.upload.services;

import edu.project.upload.configuration.UploadConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaulHashCalculator implements HashCalculator {
    private final UploadConfiguration uploadConfiguration;

    @Override
    public String calculateHash(InputStream inputStream) {
        try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            MessageDigest messageDigest = MessageDigest.getInstance(uploadConfiguration.getHashAlgo());
            while ((bytesRead = bis.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, bytesRead);
            }

            byte[] digest = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            log.debug("calculated hash: {}", sb);
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not calculate hash for file", e);
        }
    }
}
