package edu.project.upload.repositories;

import edu.project.upload.model.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Set;

public interface FileInfoRepository extends MongoRepository<FileInfo, String> {

    @Query("{ 'userId': ?0, 'tags': { $in: ?1 } }")
    Page<FileInfo> findByUserIdAndTagsInIgnoreCase(String userId, Set<String> tags, Pageable pageable);

    @Query("{ 'visibility': ?0, 'tags': { $in: ?1 } }")
    Page<FileInfo> findByVisibilityAndTagsInIgnoreCase(String visibility, Set<String> tags, Pageable pageable);

    Page<FileInfo> findByVisibility(String visibility, Pageable pageable);

    Page<FileInfo> findByUserId(String userId, Pageable pageable);

    boolean existsByUserIdAndFileName(String userId, String fileName);

    boolean existsByUserIdAndHash(String userId, String hash);
}