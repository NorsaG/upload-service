package edu.project.upload.controllers;

import edu.project.upload.model.FileInfo;
import edu.project.upload.model.Visibility;
import edu.project.upload.requests.*;
import edu.project.upload.services.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Slf4j
@RestController
@RequiredArgsConstructor
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@RequestMapping("/api/v1/files")
public class UploadFilesController {
    private final UploadService uploadService;
    private final ExecutorService executorService;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<FileInfo>> uploadFile(@ModelAttribute FileUploadRequest fileUpload) {
        log.debug("upload file request: {}", fileUpload);
        return CompletableFuture.supplyAsync(() -> {
            FileInfo fileInfo = uploadService.uploadFile(fileUpload);
            return ResponseEntity.ok(fileInfo);
        }, executorService);
    }

    @PatchMapping(path = "/{fileId}/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileInfo> rename(@PathVariable String fileId, @RequestBody FileRenameRequest fileRenameRequest) {
        log.debug("rename fileId: {}, request: {}", fileId, fileRenameRequest);
        FileInfo fileInfo = uploadService.renameFile(fileId, fileRenameRequest.getNewFileName(), fileRenameRequest.getUserId());
        return ResponseEntity.ok(fileInfo);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<FileInfo>> listFiles(@ModelAttribute FileListRequest request) {
        log.debug("list files request: {}", request);
        PageRequest pageRequest = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(request.getSortDirection(), request.getSortBy())
        );
        Page<FileInfo> files = uploadService.listFiles(request.getUserId(), Visibility.parseString(request.getVisibility()), request.getTag(), pageRequest);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/tags")
    public ResponseEntity<Set<String>> listTags(@ModelAttribute TagsListRequest tagsListRequest) {
        log.debug("list tags request: {}", tagsListRequest);
        Set<String> tags = uploadService.listTags(tagsListRequest.getUserId());
        return ResponseEntity.ok(tags);
    }

    @DeleteMapping(path = "/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId, @RequestParam String userId) {
        log.debug("delete fileId: {}, userId: {}", fileId, userId);
        uploadService.deleteFile(fileId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{fileId}/download")
    public CompletableFuture<ResponseEntity<?>> downloadFile(@PathVariable String fileId) {
        log.debug("download fileId: {}", fileId);
        return CompletableFuture.supplyAsync(() -> uploadService.downloadFile(fileId), executorService);
    }

    @PatchMapping(path = "/{fileId}/visibility", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileInfo> changeVisibility(@PathVariable String fileId, @RequestBody FileChangeVisibilityRequest request) {
        log.debug("change visibility fileId: {}, request: {}", fileId, request);
        FileInfo fileInfo = uploadService.changeVisibility(fileId, request.getUserId(), request.getVisibility());
        return ResponseEntity.ok(fileInfo);
    }
}
