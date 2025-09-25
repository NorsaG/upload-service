package edu.project.upload.services;


import java.io.InputStream;

public interface HashCalculator {
    String calculateHash(InputStream inputStream);
}
