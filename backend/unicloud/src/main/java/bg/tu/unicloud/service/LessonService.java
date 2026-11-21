package bg.tu.unicloud.service;

import bg.tu.unicloud.dto.LessonCreateRequest;
import bg.tu.unicloud.dto.LessonResponse;
import bg.tu.unicloud.dto.LessonUpdateRequest;
import bg.tu.unicloud.exception.ResourceNotFoundException;
import bg.tu.unicloud.model.Lesson;
import bg.tu.unicloud.model.Module;
import bg.tu.unicloud.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ModuleService moduleService;
    private final CourseService courseService;

    @Transactional
    public LessonResponse createLesson(UUID moduleId, LessonCreateRequest request) {
        Module module = moduleService.getModuleEntity(moduleId);
        courseService.checkCourseOwnership(module.getCourse());

        int order = request.getOrderIndex() != null ? request.getOrderIndex() : 0;

        Lesson lesson = Lesson.builder()
                .module(module)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .orderIndex(order)
                .build();

        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson created: id={}, moduleId={}", saved.getId(), moduleId);
        return mapToResponse(saved);
    }

    @Transactional
    public LessonResponse updateLesson(UUID lessonId, LessonUpdateRequest request) {
        Lesson lesson = getLessonEntity(lessonId);
        courseService.checkCourseOwnership(lesson.getModule().getCourse());

        lesson.setTitle(request.getTitle().trim());
        lesson.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }

        Lesson updated = lessonRepository.save(lesson);
        log.info("Lesson updated: id={}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteLesson(UUID lessonId) {
        Lesson lesson = getLessonEntity(lessonId);
        courseService.checkCourseOwnership(lesson.getModule().getCourse());

        lessonRepository.delete(lesson);
        log.info("Lesson deleted: id={}", lessonId);
    }

    @Transactional(readOnly = true)
    public Lesson getLessonEntity(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Занятието с ID " + lessonId + " не е намерено."));
    }

    public LessonResponse mapToResponse(Lesson lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .moduleId(lesson.getModule() != null ? lesson.getModule().getId() : null)
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .orderIndex(lesson.getOrderIndex())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
