package bg.tu.unicloud.controller;

import bg.tu.unicloud.dto.LessonCreateRequest;
import bg.tu.unicloud.dto.LessonResponse;
import bg.tu.unicloud.dto.ModuleResponse;
import bg.tu.unicloud.dto.ModuleUpdateRequest;
import bg.tu.unicloud.service.LessonService;
import bg.tu.unicloud.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Редакция на модул (собственик / админ)")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable UUID moduleId,
            @Valid @RequestBody ModuleUpdateRequest request) {
        ModuleResponse response = moduleService.updateModule(moduleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{moduleId}")
    @Operation(summary = "Изтриване на модул (собственик / админ)")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID moduleId) {
        moduleService.deleteModule(moduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{moduleId}/lessons")
    @Operation(summary = "Добавяне на занятие към модул (собственик / админ)")
    public ResponseEntity<LessonResponse> createLesson(
            @PathVariable UUID moduleId,
            @Valid @RequestBody LessonCreateRequest request) {
        LessonResponse response = lessonService.createLesson(moduleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
