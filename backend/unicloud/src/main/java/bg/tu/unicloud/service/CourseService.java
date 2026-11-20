package bg.tu.unicloud.service;

import bg.tu.unicloud.dto.*;
import bg.tu.unicloud.exception.AccessForbiddenException;
import bg.tu.unicloud.exception.ConflictException;
import bg.tu.unicloud.exception.ResourceNotFoundException;
import bg.tu.unicloud.model.Course;
import bg.tu.unicloud.model.Module;
import bg.tu.unicloud.model.Lesson;
import bg.tu.unicloud.model.Role;
import bg.tu.unicloud.model.User;
import bg.tu.unicloud.repository.CourseRepository;
import bg.tu.unicloud.repository.UserRepository;
import bg.tu.unicloud.security.CurrentUserService;
import bg.tu.unicloud.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "code", "title", "createdAt", "updatedAt");

    @Transactional(readOnly = true)
    public PagedResponse<CourseResponse> getCourses(String discipline, String teacher, String semester,
                                                   Boolean archived, Integer page, Integer size, String sort) {
        Pageable pageable = PaginationUtils.createPageable(
                page, size, sort, ALLOWED_SORT_FIELDS, "createdAt", Sort.Direction.DESC
        );

        UUID teacherId = null;
        if (teacher != null && !teacher.isBlank()) {
            try {
                teacherId = UUID.fromString(teacher.trim());
            } catch (IllegalArgumentException e) {
                // If teacher is provided as email or sub
                Optional<User> userOpt = userRepository.findByEmail(teacher.trim());
                if (userOpt.isPresent()) {
                    teacherId = userOpt.get().getId();
                }
            }
        }

        Boolean archivedFilter = archived != null ? archived : false;
        Page<Course> coursePage = courseRepository.findWithFilters(
                discipline != null && !discipline.isBlank() ? discipline.trim() : null,
                semester != null && !semester.isBlank() ? semester.trim() : null,
                teacherId,
                archivedFilter,
                pageable
        );

        List<CourseResponse> dtos = coursePage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<CourseResponse>builder()
                .content(dtos)
                .page(coursePage.getNumber())
                .size(coursePage.getSize())
                .totalElements(coursePage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseById(UUID courseId) {
        Course course = getCourseEntity(courseId);
        return mapToDetailResponse(course);
    }

    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request) {
        if (courseRepository.existsByCode(request.getCode().trim())) {
            throw new ConflictException("Вече съществува курс с код: " + request.getCode());
        }

        User teacher = currentUserService.requireCurrentUserEntity();
        if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.ADMIN) {
            throw new AccessForbiddenException("Само преподаватели и администратори могат да създават курсове.");
        }

        Course course = Course.builder()
                .code(request.getCode().trim())
                .title(request.getTitle().trim())
                .discipline(request.getDiscipline().trim())
                .semester(request.getSemester().trim())
                .description(request.getDescription())
                .externalCourseOfferingId(request.getExternalCourseOfferingId())
                .teacher(teacher)
                .archived(false)
                .build();

        Course saved = courseRepository.save(course);
        log.info("Course created: id={}, code={}, teacher={}", saved.getId(), saved.getCode(), teacher.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public CourseResponse updateCourse(UUID courseId, CourseUpdateRequest request) {
        Course course = getCourseEntity(courseId);
        checkCourseOwnership(course);

        course.setTitle(request.getTitle().trim());
        course.setDiscipline(request.getDiscipline().trim());
        course.setSemester(request.getSemester().trim());
        course.setDescription(request.getDescription());
        if (request.getArchived() != null) {
            course.setArchived(request.getArchived());
        }

        Course updated = courseRepository.save(course);
        log.info("Course updated: id={}, code={}", updated.getId(), updated.getCode());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCourse(UUID courseId) {
        Course course = getCourseEntity(courseId);
        checkCourseOwnership(course);

        courseRepository.delete(course);
        log.info("Course deleted: id={}, code={}", course.getId(), course.getCode());
    }

    @Transactional(readOnly = true)
    public Course getCourseEntity(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Курсът с ID " + courseId + " не е намерен."));
    }

    public void checkCourseOwnership(Course course) {
        if (currentUserService.isAdmin()) {
            return;
        }

        User currentUser = currentUserService.requireCurrentUserEntity();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new AccessForbiddenException("Нямате права за редакция на курс.");
        }

        if (!course.getTeacher().getId().equals(currentUser.getId())) {
            log.warn("Teacher {} attempted to modify course {} owned by {}",
                    currentUser.getId(), course.getId(), course.getTeacher().getId());
            throw new AccessForbiddenException("Нямате права за редакция на курс, който не притежавате.");
        }
    }

    public CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .externalCourseOfferingId(course.getExternalCourseOfferingId())
                .title(course.getTitle())
                .discipline(course.getDiscipline())
                .semester(course.getSemester())
                .description(course.getDescription())
                .teacherId(course.getTeacher() != null ? course.getTeacher().getId() : null)
                .teacherName(course.getTeacher() != null ? course.getTeacher().getFullName() : null)
                .archived(course.isArchived())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    public CourseDetailResponse mapToDetailResponse(Course course) {
        List<ModuleResponse> moduleResponses = new ArrayList<>();
        if (course.getModules() != null) {
            for (Module mod : course.getModules()) {
                List<LessonResponse> lessonResponses = new ArrayList<>();
                if (mod.getLessons() != null) {
                    for (Lesson lesson : mod.getLessons()) {
                        lessonResponses.add(LessonResponse.builder()
                                .id(lesson.getId())
                                .moduleId(mod.getId())
                                .title(lesson.getTitle())
                                .description(lesson.getDescription())
                                .orderIndex(lesson.getOrderIndex())
                                .createdAt(lesson.getCreatedAt())
                                .updatedAt(lesson.getUpdatedAt())
                                .build());
                    }
                }
                moduleResponses.add(ModuleResponse.builder()
                        .id(mod.getId())
                        .courseId(course.getId())
                        .title(mod.getTitle())
                        .description(mod.getDescription())
                        .orderIndex(mod.getOrderIndex())
                        .lessons(lessonResponses)
                        .createdAt(mod.getCreatedAt())
                        .updatedAt(mod.getUpdatedAt())
                        .build());
            }
        }

        return CourseDetailResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .externalCourseOfferingId(course.getExternalCourseOfferingId())
                .title(course.getTitle())
                .discipline(course.getDiscipline())
                .semester(course.getSemester())
                .description(course.getDescription())
                .teacherId(course.getTeacher() != null ? course.getTeacher().getId() : null)
                .teacherName(course.getTeacher() != null ? course.getTeacher().getFullName() : null)
                .archived(course.isArchived())
                .modules(moduleResponses)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
