package bg.tu.unicloud.storage;

import bg.tu.unicloud.exception.BadRequestException;
import bg.tu.unicloud.exception.UnsupportedFileTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FileValidationServiceTest {

    private FileValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new FileValidationService();
    }

    @Test
    @DisplayName("Valid PDF passes validation")
    void testValidPdf() {
        byte[] pdfContent = "%PDF-1.7 valid content for pdf testing".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "lecture.pdf", "application/pdf", pdfContent);

        assertDoesNotThrow(() -> validationService.validateFile(file));
        assertEquals("application/pdf", validationService.resolveMimeType("lecture.pdf"));
    }

    @Test
    @DisplayName("Valid PNG passes validation")
    void testValidPng() {
        byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "diagram.png", "image/png", pngHeader);

        assertDoesNotThrow(() -> validationService.validateFile(file));
        assertEquals("image/png", validationService.resolveMimeType("diagram.png"));
    }

    @Test
    @DisplayName("Valid JPEG passes validation")
    void testValidJpeg() {
        byte[] jpegHeader = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpegHeader);

        assertDoesNotThrow(() -> validationService.validateFile(file));
        assertEquals("image/jpeg", validationService.resolveMimeType("photo.jpg"));
    }

    @Test
    @DisplayName("Valid DOCX / PPTX (ZIP container) passes validation")
    void testValidOfficeOpenXml() {
        byte[] zipHeader = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x06, 0x00};
        MockMultipartFile docx = new MockMultipartFile("file", "assignment.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", zipHeader);
        MockMultipartFile pptx = new MockMultipartFile("file", "slides.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", zipHeader);

        assertDoesNotThrow(() -> validationService.validateFile(docx));
        assertDoesNotThrow(() -> validationService.validateFile(pptx));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", validationService.resolveMimeType("assignment.docx"));
        assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation", validationService.resolveMimeType("slides.pptx"));
    }

    @Test
    @DisplayName("Explicit executable extension (.exe) is rejected")
    void testExecutableExtensionRejected() {
        byte[] content = "MZ executable content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "malware.exe", "application/octet-stream", content);

        assertThrows(UnsupportedFileTypeException.class, () -> validationService.validateFile(file));
    }

    @Test
    @DisplayName("Disguised executable (.exe renamed to .pdf with MZ magic bytes) is rejected")
    void testDisguisedExecutableRejected() {
        byte[] mzHeader = new byte[]{0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "fake_lecture.pdf", "application/pdf", mzHeader);

        UnsupportedFileTypeException ex = assertThrows(UnsupportedFileTypeException.class, () -> validationService.validateFile(file));
        assertTrue(ex.getMessage().contains("PE/DOS"));
    }

    @Test
    @DisplayName("Disguised Linux ELF binary renamed to .docx is rejected")
    void testDisguisedElfBinaryRejected() {
        byte[] elfHeader = new byte[]{0x7F, 0x45, 0x4C, 0x46, 0x02, 0x01, 0x01, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "syllabus.docx", "application/octet-stream", elfHeader);

        UnsupportedFileTypeException ex = assertThrows(UnsupportedFileTypeException.class, () -> validationService.validateFile(file));
        assertTrue(ex.getMessage().contains("ELF"));
    }

    @Test
    @DisplayName("Disguised shell script with shebang renamed to .pdf is rejected")
    void testDisguisedShebangScriptRejected() {
        byte[] scriptContent = "#!/bin/bash\nrm -rf /".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "assignment.pdf", "application/pdf", scriptContent);

        UnsupportedFileTypeException ex = assertThrows(UnsupportedFileTypeException.class, () -> validationService.validateFile(file));
        assertTrue(ex.getMessage().contains("Shebang"));
    }

    @Test
    @DisplayName("Disguised PHP script payload renamed to .png is rejected")
    void testDisguisedPhpScriptRejected() {
        byte[] phpContent = "<?php system($_GET['cmd']); ?>".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", phpContent);

        UnsupportedFileTypeException ex = assertThrows(UnsupportedFileTypeException.class, () -> validationService.validateFile(file));
        assertTrue(ex.getMessage().contains("скриптов код"));
    }

    @Test
    @DisplayName("PDF without %PDF- header is rejected")
    void testCorruptedOrMismatchedPdfHeader() {
        byte[] invalidHeader = "THIS IS NOT A REAL PDF FILE".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "corrupt.pdf", "application/pdf", invalidHeader);

        UnsupportedFileTypeException ex = assertThrows(UnsupportedFileTypeException.class, () -> validationService.validateFile(file));
        assertTrue(ex.getMessage().contains("PDF хедър"));
    }

    @Test
    @DisplayName("File exceeding 50 MB limit throws MaxUploadSizeExceededException")
    void testOversizedFile() {
        MockMultipartFile oversized = new MockMultipartFile("file", "large.pdf", "application/pdf", new byte[10]) {
            @Override
            public long getSize() {
                return 51 * 1024 * 1024L; // 51 MB
            }
        };

        assertThrows(MaxUploadSizeExceededException.class, () -> validationService.validateFile(oversized));
    }

    @Test
    @DisplayName("Empty file throws BadRequestException")
    void testEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertThrows(BadRequestException.class, () -> validationService.validateFile(emptyFile));
    }
}
