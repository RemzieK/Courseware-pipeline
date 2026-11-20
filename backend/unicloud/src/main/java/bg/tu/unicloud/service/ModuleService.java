package bg.tu.unicloud.service;

import bg.tu.unicloud.dto.LessonResponse;
import bg.tu.unicloud.dto.ModuleCreateRequest;
import bg.tu.unicloud.dto.ModuleResponse;
import bg.tu.unicloud.dto.ModuleUpdateRequest;
import bg.tu.unicloud.exception.ResourceNotFoundException;
import bg.tu.unicloud.model.Course;
import bg.tu.unicloud.model.Lesson;
import bg.tu.unicloud.model.Module;
import bg.tu.unicloud.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseService courseService;

    @Transactional(readOnly = true)
    public List<ModuleResponse> getModulesByCourse(UUID courseId) {
        // Ensure course exists
        courseService.getCourseEntity(courseId);
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        return modules.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public ModuleResponse createModule(UUID courseId, ModuleCreateRequest request) {
        Course course = courseService.getCourseEntity(courseId);
        courseService.checkCourseOwnership(course);

        int order = request.getOrderIndex() != null ? request.getOrderIndex() : 0;

        Module module = Module.builder()
                .course(course)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .orderIndex(order)
                .build();

        Module saved = moduleRepository.save(module);
        log.info("Module created: id={}, courseId={}", saved.getId(), courseId);
        return mapToResponse(saved);
    }

    @Transactional
    public ModuleResponse updateModule(UUID moduleId, ModuleUpdateRequest request) {
        Module module = getModuleEntity(moduleId);
        courseService.checkCourseOwnership(module.getCourse());

        module.setTitle(request.getTitle().trim());
        module.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) {
            module.setOrderIndex(request.getOrderIndex());
        }

        Module updated = moduleRepository.save(module);
        log.info("Module updated: id={}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteModule(UUID moduleId) {
        Module module = getModuleEntity(moduleId);
        courseService.checkCourseOwnership(module.getCourse());

        moduleRepository.delete(module);
        log.info("Module deleted: id={}", moduleId);
    }

    @Transactional(readOnly = true)
    public Module getModuleEntity(UUID moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Модулът с ID " + moduleId + " не е намерен."));
    }

    public ModuleResponse mapToResponse(Module module) {
        List<LessonResponse> lessonResponses = new ArrayList<>();
        if (module.getLessons() != null) {
            for (Lesson lesson : module.getLessons()) {
                lessonResponses.add(LessonResponse.builder()
                        .id(lesson.getId())
                        .moduleId(module.getId())
                        .title(lesson.getTitle())
                        .description(lesson.getDescription())
                        .orderIndex(lesson.getOrderIndex())
                        .createdAt(lesson.getCreatedAt())
                        .updatedAt(lesson.getUpdatedAt())
                        .build());
            }
        }

        return ModuleResponse.builder()
                .id(module.getId())
                .courseId(module.getCourse() != null ? module.getCourse().getId() : null)
                .title(module.getTitle())
                .description(module.getDescription())
                .orderIndex(module.getOrderIndex())
                .lessons(lessonResponses)
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }
}
