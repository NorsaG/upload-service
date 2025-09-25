package edu.project.upload.services;

import com.mongodb.client.gridfs.model.GridFSFile;
import edu.project.upload.model.FileInfo;
import edu.project.upload.model.Visibility;
import edu.project.upload.repositories.FileInfoRepository;
import edu.project.upload.requests.FileUploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MongoUploadService implements UploadService {
    private final FileInfoRepository fileInfoRepository;
    private final GridFsTemplate gridFsTemplate;
    private final HashCalculator hashCalculator;
    private final MongoTemplate mongoTemplate;

    @Override
    public FileInfo uploadFile(FileUploadRequest request) {
        Set<String> tags = request.getTags() != null ? new HashSet<>(request.getTags()) : new HashSet<>();
        Set<String> normalizedTags = getNormalizedTags(tags);
        String hash = validateFileAndGetHash(request);
        String fileId = UUID.randomUUID().toString();
        String downloadLink = "/api/v1/files/" + fileId + "/download";
        String contentType = request.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = request.getFile().getContentType();
        }
        ObjectId gridFsId;
        try {
            gridFsId = gridFsTemplate.store(request.getFile().getInputStream(), request.getFileName(), contentType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
        FileInfo fileInfo = FileInfo.builder()
                .fileId(fileId)
                .userId(request.getUserId())
                .fileName(request.getFileName())
                .tags(normalizedTags)
                .fileSize(request.getFile().getSize())
                .visibility(request.getVisibility())
                .uploadDate(LocalDateTime.now())
                .hash(hash)
                .contentType(contentType)
                .downloadLink(downloadLink)
                .gridFsId(gridFsId.toHexString())
                .build();
        log.debug("Saving file info: {}", fileInfo);
        fileInfoRepository.save(fileInfo);
        log.debug("Saved file info in Mongo: {}", fileInfo);
        return fileInfo;
    }

    private String validateFileAndGetHash(FileUploadRequest request) {
        log.debug("Validating file upload request: {}", request);
        if (fileInfoRepository.existsByUserIdAndFileName(request.getUserId(), request.getFileName())) {
            throw new IllegalArgumentException("File with this filename already exists for user");
        }
        String hash;
        try {
            hash = hashCalculator.calculateHash(request.getFile().getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to calculate file hash", e);
        }
        if (fileInfoRepository.existsByUserIdAndHash(request.getUserId(), hash)) {
            throw new IllegalArgumentException("File with this content already exists for user");
        }
        return hash;
    }

    private Set<String> getNormalizedTags(Set<String> tags) {
        if (tags.size() > 5) {
            throw new IllegalArgumentException("Max 5 tags allowed");
        }
        Set<String> normalizedTags = new HashSet<>();
        for (String tag : tags) {
            normalizedTags.add(tag.toLowerCase());
        }
        return normalizedTags;
    }

    @Override
    public FileInfo renameFile(String fileId, String newFileName, String userId) {
        FileInfo fileInfo = fileInfoRepository.findById(fileId).orElseThrow();
        if (!fileInfo.getUserId().equals(userId)) {
            throw new AuthException("Not an owner");
        }
        if (fileInfoRepository.existsByUserIdAndFileName(userId, newFileName)) {
            throw new IllegalArgumentException("Filename already exists for user");
        }
        fileInfo.setFileName(newFileName);
        log.debug("Renaming file info: {}", fileInfo);
        fileInfoRepository.save(fileInfo);
        log.debug("Saved file info in Mongo: {}", fileInfo);
        return fileInfo;
    }

    @Override
    public Page<FileInfo> listFiles(String userId, Visibility visibility, String tag, PageRequest pageRequest) {
        if (visibility == Visibility.PUBLIC) {
            return listAllPublicFiles(tag, pageRequest);
        } else {
            return listAllPrivateFiles(userId, tag, pageRequest);
        }
    }

    private Page<FileInfo> listAllPublicFiles(String tag, PageRequest pageRequest) {
        if (StringUtils.hasText(tag)) {
            log.debug("finding public files with tag: {}", tag);
            return fileInfoRepository.findByVisibilityAndTagsInIgnoreCase(Visibility.PUBLIC.name(), Set.of(tag.toLowerCase()), pageRequest);
        } else {
            log.debug("finding all public files");
            return fileInfoRepository.findByVisibility(Visibility.PUBLIC.name(), pageRequest);
        }
    }

    private Page<FileInfo> listAllPrivateFiles(String userId, String tag, PageRequest pageRequest) {
        if (tag != null) {
            log.debug("finding private files for userId: {} with tag: {}", userId, tag);
            return fileInfoRepository.findByUserIdAndTagsInIgnoreCase(userId, Set.of(tag.toLowerCase()), pageRequest);
        } else {
            log.debug("finding all private files for userId: {}", userId);
            return fileInfoRepository.findByUserId(userId, pageRequest);
        }
    }

    @Override
    public void deleteFile(String fileId, String userId) {
        FileInfo fileInfo = fileInfoRepository.findById(fileId).orElseThrow();
        if (!fileInfo.getUserId().equals(userId)) {
            throw new AuthException("Not an owner");
        }
        log.debug("Deleting file info: {}", fileInfo);
        fileInfoRepository.deleteById(fileId);
        log.debug("Deleted file info in Mongo: {}", fileInfo);
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(new ObjectId(fileInfo.getGridFsId()))));
        log.debug("Deleted file from GridFS: {}", fileInfo);
    }

    @Override
    public ResponseEntity<?> downloadFile(String fileId) {
        FileInfo fileInfo = fileInfoRepository.findById(fileId).orElseThrow();
        log.debug("Downloading file info: {}", fileInfo);
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(fileInfo.getGridFsId()))));
        log.debug("Found GridFS file: {}", gridFSFile);
        GridFsResource resource = gridFsTemplate.getResource(gridFSFile);
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                    .body(new InputStreamResource(resource.getInputStream()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to read file");
        }
    }

    @Override
    public FileInfo changeVisibility(String fileId, String userId, Visibility visibility) {
        FileInfo fileInfo = fileInfoRepository.findById(fileId).orElseThrow();
        log.debug("Changing visibility for file info: {}", fileInfo);
        if (!fileInfo.getUserId().equals(userId)) {
            throw new AuthException("Not an owner");
        }
        fileInfo.setVisibility(visibility);
        log.debug("Changed visibility for file info: {}", fileInfo);
        fileInfoRepository.save(fileInfo);
        log.debug("Saved file info in Mongo: {}", fileInfo);
        return fileInfo;
    }

    @Override
    public Set<String> listTags(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("User should be specified");
        }
        return new HashSet<>(findDistinctTagsByUserId(userId));
    }

    private List<String> findDistinctTagsByUserId(String userId) {
        return mongoTemplate.query(FileInfo.class)
                .distinct("tags")
                .matching(Criteria.where("userId").is(userId))
                .as(String.class)
                .all();
    }
}
