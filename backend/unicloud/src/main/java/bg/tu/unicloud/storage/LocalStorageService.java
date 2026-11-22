package bg.tu.unicloud.storage;

import bg.tu.unicloud.exception.BadRequestException;
import bg.tu.unicloud.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;
    private final FileValidationService fileValidationService;

    public LocalStorageService(
            @Value("${storage.local.root-dir:/var/unicloud/storage}") String rootDir,
            FileValidationService fileValidationService) {
        this.rootLocation = Paths.get(rootDir).toAbsolutePath().normalize();
        this.fileValidationService = fileValidationService;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.rootLocation);
            log.info("Storage root directory initialized at: {}", this.rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage directory at {}", this.rootLocation, e);
            throw new IllegalStateException("Не може да бъде инициализирана директорията за файлово съхранение.", e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файлът за качване не може да бъде празен.");
        }

        String rawOriginalFilename = file.getOriginalFilename();
        String originalFilename = rawOriginalFilename != null
                ? Paths.get(rawOriginalFilename).getFileName().toString()
                : "file";

        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        String cleanSubDir = subDirectory != null
                ? subDirectory.replace("..", "").replaceAll("^/+", "")
                : "";

        Path targetDir = this.rootLocation.resolve(cleanSubDir).normalize();
        if (!targetDir.startsWith(this.rootLocation)) {
            throw new SecurityException("Невалиден файлов път (Path Traversal опит).");
        }

        try {
            Files.createDirectories(targetDir);
            String uniqueFilename = UUID.randomUUID() + "_" + sanitizedFilename;
            Path destinationFile = targetDir.resolve(uniqueFilename).normalize();

            if (!destinationFile.startsWith(this.rootLocation)) {
                throw new SecurityException("Невалиден файлов път за запис.");
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long bytesWritten = 0;

            try (InputStream inputStream = file.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(
                         destinationFile,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                    outputStream.write(buffer, 0, bytesRead);
                    bytesWritten += bytesRead;
                }
            }

            String sha256Hex = HexFormat.of().formatHex(digest.digest());
            String relativePath = this.rootLocation.relativize(destinationFile).toString().replace('\\', '/');
            String mimeType = fileValidationService.resolveMimeType(originalFilename);

            log.info("Stored file: path={}, size={} bytes, hash={}", relativePath, bytesWritten, sha256Hex);

            return StoredFile.builder()
                    .storagePath(relativePath)
                    .fileName(originalFilename)
                    .mimeType(mimeType)
                    .fileSizeBytes(bytesWritten)
                    .fileHash(sha256Hex)
                    .build();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 алгоритъмът не е наличен.", e);
        } catch (IOException e) {
            log.error("Failed to store file: {}", originalFilename, e);
            throw new RuntimeException("Грешка при запис на файла на диск.", e);
        }
    }

    @Override
    public Resource loadAsResource(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            throw new ResourceNotFoundException("Не е указан път към файл.");
        }

        String cleanPath = storagePath.replace("..", "").replaceAll("^/+", "");
        Path filePath = this.rootLocation.resolve(cleanPath).normalize();

        if (!filePath.startsWith(this.rootLocation)) {
            throw new SecurityException("Невалиден файлов път.");
        }

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new ResourceNotFoundException("Файлът не съществува или не е четим: " + cleanPath);
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Файлът не може да бъде прочетен: " + cleanPath);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Невалиден URI за файл: " + cleanPath);
        }
    }

    @Override
    public void delete(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }

        String cleanPath = storagePath.replace("..", "").replaceAll("^/+", "");
        Path filePath = this.rootLocation.resolve(cleanPath).normalize();

        if (!filePath.startsWith(this.rootLocation)) {
            throw new SecurityException("Невалиден файлов път.");
        }

        try {
            Files.deleteIfExists(filePath);
            log.info("Deleted file at: {}", cleanPath);
        } catch (IOException e) {
            log.warn("Could not delete file at: {}", cleanPath, e);
        }
    }
}
