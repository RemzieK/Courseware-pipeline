package bg.tu.unicloud.controller;

import bg.tu.unicloud.dto.CourseProgressResponse;
import bg.tu.unicloud.dto.ReadMarkResponse;
import bg.tu.unicloud.service.ReadMarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Progress", description = "Управление на отметки „прочетено“ и напредък по курсове")
public class ReadMarkController {

    private final ReadMarkService readMarkService;

    @PutMapping("/api/v1/materials/{materialId}/read")
    @Operation(summary = "Отбелязване на материал като прочетен (студент)")
    public ResponseEntity<ReadMarkResponse> markAsRead(@PathVariable UUID materialId) {
        ReadMarkResponse response = readMarkService.markAsRead(materialId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().cachePrivate())
                .body(response);
    }

    @DeleteMapping("/api/v1/materials/{materialId}/read")
    @Operation(summary = "Премахване на отметка „прочетено“ (студент)")
    public ResponseEntity<Void> unmarkAsRead(@PathVariable UUID materialId) {
        readMarkService.unmarkAsRead(materialId);
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.noStore().cachePrivate())
                .build();
    }

    @GetMapping("/api/v1/courses/{courseId}/progress")
    @Operation(summary = "Преглед на напредъка на студента по конкретен курс")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(@PathVariable UUID courseId) {
        CourseProgressResponse response = readMarkService.getProgress(courseId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().cachePrivate())
                .body(response);
    }
}
