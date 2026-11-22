package bg.tu.unicloud.controller;

import bg.tu.unicloud.dto.MaterialResponse;
import bg.tu.unicloud.dto.MaterialUpdateRequest;
import bg.tu.unicloud.dto.MaterialVersionCreateRequest;
import bg.tu.unicloud.dto.MaterialVersionResponse;
import bg.tu.unicloud.model.MaterialVersion;
import bg.tu.unicloud.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
@Tag(name = "Materials", description = "Управление на учебни материали и техните файлови версии")
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping("/{materialId}")
    @Operation(summary = "Детайли за материал с текущата му версия")
    public ResponseEntity<MaterialResponse> getMaterialById(@PathVariable UUID materialId) {
        MaterialResponse response = materialService.getMaterialById(materialId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(response);
    }

    @PatchMapping("/{materialId}")
    @Operation(summary = "Редакция на метаданни за материал (собственик / админ)")
    public ResponseEntity<MaterialResponse> updateMaterial(
            @PathVariable UUID materialId,
            @Valid @RequestBody MaterialUpdateRequest request) {
        MaterialResponse response = materialService.updateMaterial(materialId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{materialId}/versions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Качване на нова версия на файл към материал чрез JSON метаданни (собственик / админ)")
    public ResponseEntity<MaterialVersionResponse> addVersionJson(
            @PathVariable UUID materialId,
            @Valid @RequestBody MaterialVersionCreateRequest request) {
        MaterialVersionResponse response = materialService.addVersion(materialId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{materialId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Качване на нова версия на файл към материал чрез multipart/form-data (собственик / админ)")
    public ResponseEntity<MaterialVersionResponse> addVersionMultipart(
            @PathVariable UUID materialId,
            @RequestPart("file") MultipartFile file) {
        MaterialVersionResponse response = materialService.addVersion(materialId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{materialId}/versions")
    @Operation(summary = "История на версиите на файл към материал")
    public ResponseEntity<List<MaterialVersionResponse>> getVersions(@PathVariable UUID materialId) {
        List<MaterialVersionResponse> response = materialService.getVersions(materialId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(response);
    }

    @GetMapping("/{materialId}/file")
    @Operation(summary = "Изтегляне на файл от конкретна или текуща версия")
    public ResponseEntity<?> getFile(
            @PathVariable UUID materialId,
            @RequestParam(required = false) Integer version) {
        MaterialService.DownloadFileResult result = materialService.downloadFile(materialId, version);

        if (result.isRedirect()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(result.getRedirectUrl()))
                    .cacheControl(CacheControl.maxAge(86400, TimeUnit.SECONDS).cachePublic())
                    .build();
        }

        MaterialVersion mv = result.getMaterialVersion();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mv.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + mv.getFileName() + "\"")
                .cacheControl(CacheControl.maxAge(86400, TimeUnit.SECONDS).cachePublic())
                .body(result.getResource());
    }
}
