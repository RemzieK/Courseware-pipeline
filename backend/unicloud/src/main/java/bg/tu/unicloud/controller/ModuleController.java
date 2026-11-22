package bg.tu.unicloud.controller;

import bg.tu.unicloud.annotation.StandardApiResponses;
import bg.tu.unicloud.dto.ErrorResponse;
import bg.tu.unicloud.dto.LessonCreateRequest;
import bg.tu.unicloud.dto.LessonResponse;
import bg.tu.unicloud.dto.ModuleResponse;
import bg.tu.unicloud.dto.ModuleUpdateRequest;
import bg.tu.unicloud.service.LessonService;
import bg.tu.unicloud.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
@Tag(name = "Modules", description = "Управление на модули (тематични седмици) в курсове")
public class ModuleController {

    private final ModuleService moduleService;
    private final LessonService lessonService;

    @PutMapping("/{moduleId}")
    @Operation(summary = "Редакция на модул (собственик / админ)",
            description = "Изисква роля TEACHER (само собственик) или ADMIN.")
    @ApiResponse(responseCode = "200", description = "Модулът е актуализиран успешно",
            content = @Content(schema = @Schema(implementation = ModuleResponse.class)))
    @StandardApiResponses
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable UUID moduleId,
            @Valid @RequestBody ModuleUpdateRequest request) {
        ModuleResponse response = moduleService.updateModule(moduleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{moduleId}")
    @Operation(summary = "Изтриване на модул (собственик / админ)",
            description = "Изисква роля TEACHER (само собственик) или ADMIN. Каскадно изтрива занятия и материали.")
    @ApiResponse(responseCode = "204", description = "Модулът е изтрит успешно")
    @ApiResponse(responseCode = "401", description = "Липсващ или невалиден токен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Недостатъчни права",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Модулът не е намерен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> deleteModule(@PathVariable UUID moduleId) {
        moduleService.deleteModule(moduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{moduleId}/lessons")
    @Operation(summary = "Добавяне на занятие към модул (собственик / админ)",
            description = "Изисква роля TEACHER (само собственик) или ADMIN.")
    @ApiResponse(responseCode = "201", description = "Занятието е създадено успешно",
            content = @Content(schema = @Schema(implementation = LessonResponse.class)))
    @StandardApiResponses
    public ResponseEntity<LessonResponse> createLesson(
            @PathVariable UUID moduleId,
            @Valid @RequestBody LessonCreateRequest request) {
        LessonResponse response = lessonService.createLesson(moduleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
