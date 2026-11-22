package bg.tu.unicloud.storage;

import bg.tu.unicloud.exception.BadRequestException;
import bg.tu.unicloud.exception.UnsupportedFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
public class FileValidationService {

    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024L; // 50 MB

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "pptx", "ppt", "docx", "doc", "png", "jpg", "jpeg"
    );

    private static final Set<String> PROHIBITED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "bash", "ps1", "php", "phtml",
            "jsp", "js", "jar", "war", "py", "pl", "com", "scr", "vbs",
            "msi", "dll", "so", "bin", "cgi"
    );

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файлът не може да бъде празен.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new MaxUploadSizeExceededException(MAX_FILE_SIZE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new UnsupportedFileTypeException("Файлът няма разширение или валидно име.");
        }

        String extension = extractExtension(originalFilename);

        if (PROHIBITED_EXTENSIONS.contains(extension)) {
            throw new UnsupportedFileTypeException("Качването на изпълними файлове и скриптове (" + extension + ") е строго забранено.");
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new UnsupportedFileTypeException("Неподдържан файлов формат (." + extension + "). Разрешени формати: PDF, PPTX, DOCX, PNG, JPG.");
        }

        validateMagicBytes(file, extension);
    }

    private void validateMagicBytes(MultipartFile file, String extension) {
        byte[] header = new byte[512];
        int bytesRead;
        try (InputStream inputStream = file.getInputStream()) {
            bytesRead = inputStream.read(header);
        } catch (IOException e) {
            throw new BadRequestException("Грешка при четене на съдържанието на файла: " + e.getMessage());
        }

        if (bytesRead < 4) {
            throw new UnsupportedFileTypeException("Файлът е твърде малък или повреден.");
        }

        // 1. Detect executable / script signatures (reject even if renamed as .pdf, .docx, etc.)
        // Windows PE / DOS executable header: "MZ" (0x4D, 0x5A)
        if (header[0] == 0x4D && header[1] == 0x5A) {
            throw new UnsupportedFileTypeException("Открит е изпълним Windows (PE/DOS MZ) файл. Качването е забранено.");
        }

        // Unix ELF binary header: 0x7F 'E' 'L' 'F'
        if (header[0] == 0x7F && header[1] == 0x45 && header[2] == 0x4C && header[3] == 0x46) {
            throw new UnsupportedFileTypeException("Открит е изпълним Linux/Unix (ELF) файл. Качването е забранено.");
        }

        // Shebang script: "#!" (0x23, 0x21)
        if (header[0] == 0x23 && header[1] == 0x21) {
            throw new UnsupportedFileTypeException("Открит е изпълним скрипт (Shebang #!). Качването е забранено.");
        }

        // Check text-based script payloads in the header (e.g., <?php, <script, <html)
        String headerStr = new String(header, 0, bytesRead, StandardCharsets.ISO_8859_1).toLowerCase(Locale.ROOT);
        if (headerStr.contains("<?php") || headerStr.contains("<script") || headerStr.contains("<html")) {
            throw new UnsupportedFileTypeException("Открит е скриптов код или непозволен HTML в съдържанието на файла.");
        }

        // 2. Format specific validation
        switch (extension) {
            case "pdf" -> {
                // PDF magic bytes: "%PDF-" (0x25, 0x50, 0x44, 0x46, 0x2D)
                if (bytesRead < 5 ||
                        header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46 || header[4] != 0x2D) {
                    throw new UnsupportedFileTypeException("Файлът няма валиден PDF хедър (%PDF-).");
                }
            }
            case "png" -> {
                // PNG magic bytes: \x89PNG\r\n\x1a\n
                if (bytesRead < 8 ||
                        header[0] != (byte) 0x89 || header[1] != 0x50 || header[2] != 0x4E || header[3] != 0x47 ||
                        header[4] != 0x0D || header[5] != 0x0A || header[6] != 0x1A || header[7] != 0x0A) {
                    throw new UnsupportedFileTypeException("Файлът няма валиден PNG хедър.");
                }
            }
            case "jpg", "jpeg" -> {
                // JPEG magic bytes: 0xFF, 0xD8, 0xFF
                if (header[0] != (byte) 0xFF || header[1] != (byte) 0xD8 || header[2] != (byte) 0xFF) {
                    throw new UnsupportedFileTypeException("Файлът няма валиден JPEG хедър.");
                }
            }
            case "docx", "pptx" -> {
                // Office Open XML formats are ZIP archives starting with PK\x03\x04
                if (header[0] != 0x50 || header[1] != 0x4B || header[2] != 0x03 || header[3] != 0x04) {
                    throw new UnsupportedFileTypeException("Файлът няма валиден Office Open XML (ZIP) хедър.");
                }
            }
            case "doc", "ppt" -> {
                // Microsoft Compound File Binary Format (OLE2) or ZIP
                boolean isOle2 = (header[0] == (byte) 0xD0 && header[1] == (byte) 0xCF && header[2] == 0x11 && header[3] == (byte) 0xE0);
                boolean isZip = (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04);
                if (!isOle2 && !isZip) {
                    throw new UnsupportedFileTypeException("Файлът няма валиден Microsoft Office хедър.");
                }
            }
            default -> throw new UnsupportedFileTypeException("Неподдържан файлов формат: " + extension);
        }
    }

    public String resolveMimeType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String ext = extractExtension(filename);
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "doc" -> "application/msword";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
