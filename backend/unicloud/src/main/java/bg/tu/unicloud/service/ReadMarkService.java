package bg.tu.unicloud.service;

import bg.tu.unicloud.dto.CourseProgressResponse;
import bg.tu.unicloud.dto.ReadMarkResponse;
import bg.tu.unicloud.exception.AccessForbiddenException;
import bg.tu.unicloud.exception.ResourceNotFoundException;
import bg.tu.unicloud.model.Course;
import bg.tu.unicloud.model.Material;
import bg.tu.unicloud.model.ReadMark;
import bg.tu.unicloud.model.Role;
import bg.tu.unicloud.model.User;
import bg.tu.unicloud.repository.MaterialRepository;
import bg.tu.unicloud.repository.ReadMarkRepository;
import bg.tu.unicloud.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadMarkService {

    private final ReadMarkRepository readMarkRepository;
    private final MaterialRepository materialRepository;
    private final CourseService courseService;
    private final CurrentUserService currentUserService;

    @Transactional
    public ReadMarkResponse markAsRead(UUID materialId) {
        User currentUser = currentUserService.requireCurrentUserEntity();
        if (currentUser.getRole() != Role.STUDENT && currentUser.getRole() != Role.ADMIN) {
            throw new AccessForbiddenException("Само студенти могат да отбелязват материали като прочетени.");
        }

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Материалът с ID " + materialId + " не е намерен."));

        if (material.isHidden() && !currentUserService.isAdmin()) {
            throw new ResourceNotFoundException("Материалът с ID " + materialId + " не е намерен.");
        }

        Optional<ReadMark> existingOpt = readMarkRepository.findByUserIdAndMaterialId(currentUser.getId(), materialId);
        ReadMark readMark;
        if (existingOpt.isPresent()) {
            readMark = existingOpt.get();
            readMark.setVersionAtRead(material.getCurrentVersion());
        } else {
            readMark = ReadMark.builder()
                    .user(currentUser)
                    .material(material)
                    .versionAtRead(material.getCurrentVersion())
                    .build();
        }

        ReadMark saved = readMarkRepository.save(readMark);
        log.info("Material marked as read: materialId={}, userId={}, version={}",
                materialId, currentUser.getId(), material.getCurrentVersion());

        return mapToResponse(saved);
    }

    @Transactional
    public void unmarkAsRead(UUID materialId) {
        User currentUser = currentUserService.requireCurrentUserEntity();
        if (currentUser.getRole() != Role.STUDENT && currentUser.getRole() != Role.ADMIN) {
            throw new AccessForbiddenException("Само студенти могат да премахват отметки за прочетено.");
        }

        readMarkRepository.deleteByUserIdAndMaterialId(currentUser.getId(), materialId);
        log.info("Material read mark removed: materialId={}, userId={}", materialId, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public CourseProgressResponse getProgress(UUID courseId) {
        User currentUser = currentUserService.requireCurrentUserEntity();
        Course course = courseService.getCourseEntity(courseId);

        List<ReadMark> readMarks = readMarkRepository.findByUserIdAndCourseId(currentUser.getId(), courseId);
        long totalMaterials = materialRepository.countNonHiddenMaterialsByCourseId(courseId);

        long upToDateReadCount = readMarks.stream()
                .filter(rm -> rm.getMaterial() == null || rm.getVersionAtRead() >= rm.getMaterial().getCurrentVersion())
                .count();

        double progressPct = totalMaterials > 0
                ? ((double) upToDateReadCount / (double) totalMaterials) * 100.0
                : 0.0;

        List<ReadMarkResponse> dtos = readMarks.stream().map(this::mapToResponse).toList();

        return CourseProgressResponse.builder()
                .courseId(course.getId())
                .courseCode(course.getCode())
                .totalMaterials((int) totalMaterials)
                .readMaterials((int) upToDateReadCount)
                .progressPercentage(Math.round(progressPct * 10.0) / 10.0)
                .readMarks(dtos)
                .build();
    }

    public ReadMarkResponse mapToResponse(ReadMark readMark) {
        boolean hasNewVersion = false;
        if (readMark.getMaterial() != null) {
            hasNewVersion = readMark.getVersionAtRead() < readMark.getMaterial().getCurrentVersion();
        }

        return ReadMarkResponse.builder()
                .id(readMark.getId())
                .userId(readMark.getUser() != null ? readMark.getUser().getId() : null)
                .materialId(readMark.getMaterial() != null ? readMark.getMaterial().getId() : null)
                .versionAtRead(readMark.getVersionAtRead())
                .hasNewVersion(hasNewVersion)
                .readAt(readMark.getReadAt())
                .build();
    }
}
