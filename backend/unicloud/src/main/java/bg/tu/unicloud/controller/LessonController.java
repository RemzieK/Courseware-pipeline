package bg.tu.unicloud.controller;

import bg.tu.unicloud.annotation.StandardApiResponses;
import bg.tu.unicloud.dto.ErrorResponse;
import bg.tu.unicloud.dto.LessonResponse;
import bg.tu.unicloud.dto.LessonUpdateRequest;
import bg.tu.unicloud.dto.MaterialCreateRequest;
import bg.tu.unicloud.dto.MaterialResponse;
import bg.tu.unicloud.exception.BadRequestException;
import bg.tu.unicloud.model.MaterialType;
import bg.tu.unicloud.service.LessonService;
import bg.tu.unicloud.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "Управление на учебни занятия и материали към тях")
public class LessonController {

    private final LessonService lessonService;
    private final MaterialService materialService;

    @PutMapping("/{lessonId}")
    @Operation(summary = "Редакция на занятие (собственик / админ)",
            description = "Изисква роля TEACHER (само собственик) или ADMIN.")
    @ApiResponse(responseCode = "200", description = "Занятието е актуализирано успешно",
            content = @Content(schema = @Schema(implementation = LessonResponse.class)))
    @StandardApiResponses
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonUpdateRequest request) {
        LessonResponse response = lessonService.updateLesson(lessonId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{lessonId}")
    @Operation(summary = "Изтриване на занятие (собственик / админ)",
            description = "Изисква роля TEACHER (само собственик) или ADMIN. Каскадно изтрива материали.")
    @ApiResponse(responseCode = "204", description = "Занятието е изтрито успешно")
    @ApiResponse(responseCode = "401", description = "Липсващ или невалиден токен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Недостатъчни права",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Занятието не е намерено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{lessonId}/materials")
    @Operation(summary = "Списък с учебни материали към занятие",
            description = "Публичен ендпойнт. Скрити материали са видими само за TEACHER и ADMIN.")
    @ApiResponse(responseCode = "200", description = "Успешно извлечен списък с материали",
            content = @Content(schema = @Schema(implementation = MaterialResponse.class)))
    @ApiResponse(responseCode = "404", description = "Занятието не е намерено",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<List<MaterialResponse>> getMaterialsByLesson(@PathVariable UUID lessonId) {
        List<MaterialResponse> response = materialService.getMaterialsByLesson(lessonId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(response);
    }

    @PostMapping(value = "/{lessonId}/materials", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавяне на учебен материал чрез JSON (собственик / админ)",
            description = "Изисква роля TEACHER или ADMIN. Използва се за VIDEO_LINK или URL-базирани файлове.")
    @ApiResponse(responseCode = "201", description = "Материалът е създаден успешно",
            content = @Content(schema = @Schema(implementation = MaterialResponse.class)))
    @StandardApiResponses
    public ResponseEntity<MaterialResponse> createMaterialJson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody MaterialCreateRequest request) {
        MaterialResponse response = materialService.createMaterial(lessonId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{lessonId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавяне на учебен материал с качване на файл (собственик / админ)",
            description = "Изисква роля TEACHER или ADMIN. Поддържани типове файлове: PDF, DOCX, PPTX, MP4 (макс. 50 MB).")
    @ApiResponse(responseCode = "201", description = "Материалът е създаден и файлът е качен успешно",
            content = @Content(schema = @Schema(implementation = MaterialResponse.class)))
    @StandardApiResponses
    public ResponseEntity<MaterialResponse> createMaterialMultipart(
            @PathVariable UUID lessonId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "request", required = false) @Valid MaterialCreateRequest requestPart,
            @RequestParam(value = "type", required = false) MaterialType type,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "videoUrl", required = false) String videoUrl,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "hidden", required = false) Boolean hidden) {

        MaterialCreateRequest request = requestPart;
        if (request == null) {
            if (title == null || title.isBlank()) {
                throw new BadRequestException("Заглавието на материала е задължително.");
            }
            if (type == null) {
                throw new BadRequestException("Типът на материала е задължителен.");
            }
            request = MaterialCreateRequest.builder()
                    .type(type)
                    .title(title)
                    .description(description)
                    .videoUrl(videoUrl)
                    .tags(tags)
                    .hidden(hidden)
                    .build();
        }

        MaterialResponse response = materialService.createMaterial(lessonId, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
