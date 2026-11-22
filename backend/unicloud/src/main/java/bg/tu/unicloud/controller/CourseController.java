package bg.tu.unicloud.controller;

import bg.tu.unicloud.annotation.StandardApiResponses;
import bg.tu.unicloud.dto.*;
import bg.tu.unicloud.service.CourseService;
import bg.tu.unicloud.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Управление на курсове и техните модули")
public class CourseController {

    private final CourseService courseService;
    private final ModuleService moduleService;

    @GetMapping
    @Operation(summary = "Списък с курсове (с филтри и пагинация)",
            description = "Връща странициран списък с курсове. Всички филтри са опционални. Публичен ендпойнт – не изисква токен.")
    @ApiResponse(responseCode = "200", description = "Успешно извлечен списък с курсове",
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    public ResponseEntity<PagedResponse<CourseResponse>> getCourses(
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String teacher,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort) {

        PagedResponse<CourseResponse> response = courseService.getCourses(
                discipline, teacher, semester, archived, page, size, sort
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(response);
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Детайли за курс с модули и занятия",
            description = "Връща пълния курс с неговите модули и занятия. Публичен ендпойнт.")
    @ApiResponse(responseCode = "200", description = "Успешно извлечен курс",
            content = @Content(schema = @Schema(implementation = CourseDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "Курсът не е намерен",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<CourseDetailResponse> getCourseById(@PathVariable UUID courseId) {
        CourseDetailResponse response = courseService.getCourseById(courseId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(response);
    }

    @PostMapping
    @Operation(summary = "Създаване на нов курс (преподавател / админ)",
            description = "Изисква роля TEACHER или ADMIN. Кодът на курса трябва да е уникален.")
    @ApiResponse(responseCode = "201", description = "Курсът е създаден успешно",
            content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @StandardApiResponses
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "Редакция на курс (собственик / админ)",
            description = "Изисква роля TEACHER (само собственик) или ADMIN. Заменя всички редактируеми полета.")
    @ApiResponse(responseCode = "200", description = "Курсът е актуализиран успешно",
            content = @Content(schema = @Schema(implementation = CourseResponse.class)))
    @StandardApiResponses
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseUpdateRequest request) {
        CourseResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "Изтриване на курс (собственик / админ)",
            description = "Изисква роля TEACHER (само собственик) или ADMIN. Каскадно изтрива модули, занятия и материали.")
    @ApiResponse(responseCode = "204", description = "Курсът е изтрит успешно")
    @StandardApiResponses
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/modules")
    @Operation(summary = "Добавяне на модул към курс (собственик / админ)",
            description = "Изисква роля TEACHER (само собственик) или ADMIN.")
    @ApiResponse(responseCode = "201", description = "Модулът е създаден успешно",
            content = @Content(schema = @Schema(implementation = ModuleResponse.class)))
    @StandardApiResponses
    public ResponseEntity<ModuleResponse> createModule(
            @PathVariable UUID courseId,
            @Valid @RequestBody ModuleCreateRequest request) {
        ModuleResponse response = moduleService.createModule(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
