package edu.project.upload.integrations;

import edu.project.upload.model.Visibility;
import edu.project.upload.repositories.FileInfoRepository;
import edu.project.upload.services.AuthException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import edu.project.upload.model.FileInfo;
import edu.project.upload.requests.FileUploadRequest;
import edu.project.upload.services.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestUpload {
    @Autowired
    private UploadService uploadService;

    @Autowired
    private FileInfoRepository repository;

    @AfterAll
    public void after() {
        repository.deleteAll();
    }

    private String randomString(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    private String randomTag() {
        return "tag" + ThreadLocalRandom.current().nextInt(1, 100);
    }

    @Test
    public void testUploadSameFileName() {
        MockMultipartFile file1 = new MockMultipartFile("file", "test.txt", "text/plain", randomString("content").getBytes());
        FileUploadRequest req1 = new FileUploadRequest("user1", "test.txt", Visibility.PUBLIC, List.of("tag1"), "text/plain", file1);
        uploadService.uploadFile(req1);
        MockMultipartFile file2 = new MockMultipartFile("file", "test.txt", "text/plain", randomString("content").getBytes());
        FileUploadRequest req2 = new FileUploadRequest("user1", "test.txt", Visibility.PUBLIC, List.of("tag2"), "text/plain", file2);
        assertThrows(IllegalArgumentException.class, () -> uploadService.uploadFile(req2));
    }

    @Test
    public void testUploadSameContent() {
        MockMultipartFile file1 = new MockMultipartFile("file", "fileA.txt", "text/plain", "same_content".getBytes());
        FileUploadRequest req1 = new FileUploadRequest("user1", "fileA.txt", Visibility.PUBLIC, List.of("tag1"), "text/plain", file1);
        uploadService.uploadFile(req1);
        MockMultipartFile file2 = new MockMultipartFile("file", "fileB.txt", "text/plain", "same_content".getBytes());
        FileUploadRequest req2 = new FileUploadRequest("user1", "fileB.txt", Visibility.PUBLIC, List.of("tag2"), "text/plain", file2);
        assertThrows(IllegalArgumentException.class, () -> uploadService.uploadFile(req2));
    }

    @Test
    public void testUploadBigFile() {
        byte[] bigContent = new byte[100 * 1024 * 1024];
        MockMultipartFile bigFile = new MockMultipartFile("file", "big.bin", "application/octet-stream", bigContent);
        FileUploadRequest req = new FileUploadRequest("user1", "big.bin", Visibility.PRIVATE, List.of("big"), "application/octet-stream", bigFile);
        assertDoesNotThrow(() -> uploadService.uploadFile(req));
    }

    @Test
    public void testDeleteOwnFile() {
        MockMultipartFile file = new MockMultipartFile("file", "delete.txt", "text/plain", "delete_content".getBytes());
        FileUploadRequest req = new FileUploadRequest("user2", "delete.txt", Visibility.PRIVATE, List.of("tag"), "text/plain", file);
        FileInfo info = uploadService.uploadFile(req);
        assertDoesNotThrow(() -> uploadService.deleteFile(info.getFileId(), "user2"));
    }

    @Test
    public void testDeleteFileNotOwned() {
        MockMultipartFile file = new MockMultipartFile("file", "notowned.txt", "text/plain", "not_owned".getBytes());
        FileUploadRequest req = new FileUploadRequest("user3", "notowned.txt", Visibility.PRIVATE, List.of("tag"), "text/plain", file);
        FileInfo info = uploadService.uploadFile(req);
        assertThrows(AuthException.class, () -> uploadService.deleteFile(info.getFileId(), "user4"));
    }

    @Test
    public void testListPublicFiles() {
        // to avoid interactions with other tests
        repository.deleteAll();

        MockMultipartFile file1 = new MockMultipartFile("file", "list_public1.txt", "text/plain", randomString("content").getBytes());
        FileUploadRequest req1 = new FileUploadRequest("unique_user", "list_public1.txt", Visibility.PUBLIC, List.of("tag1"), "text/plain", file1);
        uploadService.uploadFile(req1);

        MockMultipartFile file2 = new MockMultipartFile("file", "private1.txt", "text/plain", randomString("content").getBytes());
        FileUploadRequest req2 = new FileUploadRequest("unique_user", "private1.txt", Visibility.PRIVATE, List.of("tag2"), "text/plain", file2);
        uploadService.uploadFile(req2);

        var publicFiles = uploadService.listFiles(null, Visibility.PUBLIC, "tag1", PageRequest.of(0, 10));
        assertEquals(1, publicFiles.getTotalElements());
        assertEquals("list_public1.txt", publicFiles.getContent().get(0).getFileName());
    }

    @Test
    public void testRenameFile() {
        MockMultipartFile file = new MockMultipartFile("file", "original.txt", "text/plain", "content".getBytes());
        FileUploadRequest req = new FileUploadRequest("user1", "original.txt", Visibility.PUBLIC, List.of("tag1"), "text/plain", file);
        FileInfo uploadedFile = uploadService.uploadFile(req);

        FileInfo renamedFile = uploadService.renameFile(uploadedFile.getFileId(), "renamed.txt", "user1");
        assertEquals("renamed.txt", renamedFile.getFileName());
    }

    @Test
    public void testDeleteFile() {
        MockMultipartFile file = new MockMultipartFile("file", "for_delete.txt", "text/plain", randomString("content").getBytes());
        FileUploadRequest req = new FileUploadRequest("user1", "for_delete.txt", Visibility.PUBLIC, List.of("tag1"), "text/plain", file);
        FileInfo uploadedFile = uploadService.uploadFile(req);

        uploadService.deleteFile(uploadedFile.getFileId(), "user1");
        assertFalse(repository.findById(uploadedFile.getFileId()).isPresent());
    }

    @Test
    public void testDownloadFile() {
        MockMultipartFile file = new MockMultipartFile("file", "download.txt", "text/plain", "fixed content".getBytes());
        FileUploadRequest req = new FileUploadRequest("user1", "download.txt", Visibility.PUBLIC, List.of("tag1"), "text/plain", file);
        FileInfo uploadedFile = uploadService.uploadFile(req);

        ResponseEntity<?> response = uploadService.downloadFile(uploadedFile.getFileId());
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(InputStreamResource.class, response.getBody());

        try (InputStream inputStream = ((InputStreamResource) response.getBody()).getInputStream()) {
            assertNotNull(inputStream);
            byte[] content = inputStream.readAllBytes();
            assertArrayEquals("fixed content".getBytes(), content);
        } catch (IOException e) {
            fail("Failed to read InputStreamResource: " + e.getMessage());
        }
    }

    @Test
    public void testTagCreationAndCaseInsensitiveFilter() {
        MockMultipartFile file = new MockMultipartFile("file", "tag_test.txt", "text/plain", "tag_content".getBytes());
        FileUploadRequest req = new FileUploadRequest("user8", "tag_test.txt", Visibility.PUBLIC, List.of("tag_case"), "text/plain", file);
        uploadService.uploadFile(req);
        Page<FileInfo> page = uploadService.listFiles("user8", Visibility.PUBLIC, "tag_case", PageRequest.of(0, 10));
        assertTrue(page.getContent().stream().anyMatch(f -> f.getFileName().equals("tag_test.txt")));
    }

    @Test
    public void testPaginationAndSorting() {
        for (int i = 1; i <= 15; i++) {
            String fileName = randomString("file") + ".txt";
            MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", randomString("content").getBytes());
            FileUploadRequest req = new FileUploadRequest(randomString("user"), fileName, Visibility.PUBLIC, List.of(randomTag()), "text/plain", file);
            uploadService.uploadFile(req);
        }

        var page = uploadService.listFiles(null, Visibility.PUBLIC, null, PageRequest.of(0, 5, Sort.by("fileName")));
        assertEquals(5, page.getContent().size());
    }

    @Test
    public void testChangeVisibility() {
        String fileName = randomString("visibility") + ".txt";
        MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", randomString("content").getBytes());
        FileUploadRequest req = new FileUploadRequest(randomString("user"), fileName, Visibility.PRIVATE, List.of(randomTag()), "text/plain", file);
        FileInfo uploadedFile = uploadService.uploadFile(req);

        FileInfo updatedFile = uploadService.changeVisibility(uploadedFile.getFileId(), uploadedFile.getUserId(), Visibility.PUBLIC);
        assertEquals(Visibility.PUBLIC, updatedFile.getVisibility());
    }

    @Test
    public void testFilterByTags() {
        String fileName = randomString("tag") + ".txt";
        MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", randomString("content").getBytes());
        FileUploadRequest req = new FileUploadRequest(randomString("user"), fileName, Visibility.PUBLIC, List.of(randomTag(), randomTag()), "text/plain", file);
        uploadService.uploadFile(req);

        var page = uploadService.listFiles(null, Visibility.PUBLIC, "tag1", PageRequest.of(0, 10));
        assertEquals(1, page.getContent().size());
    }

    @Test
    public void testPreventDuplicateUploads() {
        String fileName = randomString("duplicate") + ".txt";
        MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", "duplicate content".getBytes());
        FileUploadRequest req = new FileUploadRequest("user1", fileName, Visibility.PUBLIC, List.of(randomTag()), "text/plain", file);
        uploadService.uploadFile(req);

        MockMultipartFile duplicateFile = new MockMultipartFile("file", fileName, "text/plain", "duplicate content".getBytes());
        FileUploadRequest duplicateReq = new FileUploadRequest("user1", fileName, Visibility.PUBLIC, List.of(randomTag()), "text/plain", duplicateFile);
        assertThrows(IllegalArgumentException.class, () -> uploadService.uploadFile(duplicateReq));
    }

    @Test
    public void testUnauthorizedFileDeletion() {
        String fileName = randomString("unauthorized") + ".txt";
        MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", randomString("content").getBytes());
        FileUploadRequest req = new FileUploadRequest(randomString("user"), fileName, Visibility.PUBLIC, List.of(randomTag()), "text/plain", file);
        FileInfo uploadedFile = uploadService.uploadFile(req);

        assertThrows(AuthException.class, () -> uploadService.deleteFile(uploadedFile.getFileId(), randomString("user")));
    }

    @Test
    public void testInvalidFileDownload() {
        assertThrows(NoSuchElementException.class, () -> uploadService.downloadFile(randomString("nonexistent-file-id")));
    }
}
