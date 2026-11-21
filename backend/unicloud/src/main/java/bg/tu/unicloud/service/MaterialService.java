package bg.tu.unicloud.service;

import bg.tu.unicloud.dto.*;
import bg.tu.unicloud.exception.AccessForbiddenException;
import bg.tu.unicloud.exception.ResourceNotFoundException;
import bg.tu.unicloud.model.*;
import bg.tu.unicloud.repository.MaterialRepository;
import bg.tu.unicloud.repository.MaterialVersionRepository;
import bg.tu.unicloud.repository.TagRepository;
import bg.tu.unicloud.security.AuthenticatedUser;
import bg.tu.unicloud.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialVersionRepository materialVersionRepository;
    private final TagRepository tagRepository;
    private final LessonService lessonService;
    private final CourseService courseService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<MaterialResponse> getMaterialsByLesson(UUID lessonId) {
        Lesson lesson = lessonService.getLessonEntity(lessonId);
        boolean canSeeHidden = canViewHidden(lesson.getModule().getCourse());

        List<Material> materials = canSeeHidden
                ? materialRepository.findByLessonId(lessonId)
                : materialRepository.findByLessonIdAndHiddenFalse(lessonId);

        return materials.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse getMaterialById(UUID materialId) {
        Material material = materialRepository.findByIdWithDetails(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Материалът с ID " + materialId + " не е намерен."));

        if (material.isHidden() && !canViewHidden(material.getLesson().getModule().getCourse())) {
            throw new ResourceNotFoundException("Материалът с ID " + materialId + " не е намерен.");
        }

        return mapToResponse(material);
    }

    @Transactional
    public MaterialResponse createMaterial(UUID lessonId, MaterialCreateRequest request) {
        Lesson lesson = lessonService.getLessonEntity(lessonId);
        courseService.checkCourseOwnership(lesson.getModule().getCourse());

        Set<Tag> tags = resolveTags(request.getTags());

        Material material = Material.builder()
                .lesson(lesson)
                .type(request.getType())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .videoUrl(request.getVideoUrl())
                .currentVersion(1)
                .hidden(Boolean.TRUE.equals(request.getHidden()))
                .tags(tags)
                .build();

        Material saved = materialRepository.save(material);

        if (request.getFileUrl() != null && !request.getFileUrl().isBlank()) {
            User uploader = currentUserService.requireCurrentUserEntity();
            MaterialVersion version = MaterialVersion.builder()
                    .material(saved)
                    .versionNumber(1)
                    .fileUrl(request.getFileUrl().trim())
                    .fileName(request.getFileName() != null ? request.getFileName() : "file")
                    .mimeType(request.getMimeType() != null ? request.getMimeType() : "application/octet-stream")
                    .fileSizeBytes(request.getFileSizeBytes() != null ? request.getFileSizeBytes() : 0L)
                    .fileHash(request.getFileHash())
                    .uploadedBy(uploader)
                    .build();
            materialVersionRepository.save(version);
            saved.getVersions().add(version);
        }

        log.info("Material created: id={}, lessonId={}, type={}", saved.getId(), lessonId, saved.getType());
        return mapToResponse(saved);
    }

    @Transactional
    public MaterialResponse updateMaterial(UUID materialId, MaterialUpdateRequest request) {
        Material material = getMaterialEntity(materialId);
        courseService.checkCourseOwnership(material.getLesson().getModule().getCourse());

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            material.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            material.setDescription(request.getDescription());
        }
        if (request.getVideoUrl() != null) {
            material.setVideoUrl(request.getVideoUrl());
        }
        if (request.getHidden() != null) {
            material.setHidden(request.getHidden());
        }
        if (request.getTags() != null) {
            material.setTags(resolveTags(request.getTags()));
        }

        Material updated = materialRepository.save(material);
        log.info("Material updated: id={}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public MaterialVersionResponse addVersion(UUID materialId, MaterialVersionCreateRequest request) {
        Material material = getMaterialEntity(materialId);
        courseService.checkCourseOwnership(material.getLesson().getModule().getCourse());

        User uploader = currentUserService.requireCurrentUserEntity();
        int newVersionNumber = material.getCurrentVersion() + 1;
        material.setCurrentVersion(newVersionNumber);

        MaterialVersion version = MaterialVersion.builder()
                .material(material)
                .versionNumber(newVersionNumber)
                .fileUrl(request.getFileUrl().trim())
                .fileName(request.getFileName().trim())
                .mimeType(request.getMimeType().trim())
                .fileSizeBytes(request.getFileSizeBytes())
                .fileHash(request.getFileHash())
                .uploadedBy(uploader)
                .build();

        MaterialVersion saved = materialVersionRepository.save(version);
        materialRepository.save(material);

        log.info("Material version added: materialId={}, version={}", materialId, newVersionNumber);
        return mapToVersionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MaterialVersionResponse> getVersions(UUID materialId) {
        Material material = getMaterialEntity(materialId);
        if (material.isHidden() && !canViewHidden(material.getLesson().getModule().getCourse())) {
            throw new ResourceNotFoundException("Материалът с ID " + materialId + " не е намерен.");
        }

        List<MaterialVersion> versions = materialVersionRepository.findByMaterialIdOrderByVersionNumberDesc(materialId);
        return versions.stream().map(this::mapToVersionResponse).toList();
    }

    @Transactional(readOnly = true)
    public MaterialVersionResponse getFileMetadata(UUID materialId, Integer version) {
        Material material = getMaterialEntity(materialId);

        Optional<AuthenticatedUser> authUserOpt = currentUserService.getAuthenticatedUser();
        if (authUserOpt.isPresent()) {
            AuthenticatedUser authUser = authUserOpt.get();
            if (authUser.getRole() == Role.SERVICE) {
                // Strict check: SERVICE role can only download ASSIGNMENT files!
                if (material.getType() != MaterialType.ASSIGNMENT) {
                    throw new AccessForbiddenException("Служебният акаунт има достъп само до файлове на материали от тип ASSIGNMENT.");
                }
            }
        }

        if (material.isHidden() && !canViewHidden(material.getLesson().getModule().getCourse())) {
            throw new ResourceNotFoundException("Материалът с ID " + materialId + " не е намерен.");
        }

        int targetVersion = version != null ? version : material.getCurrentVersion();
        MaterialVersion materialVersion = materialVersionRepository.findByMaterialIdAndVersionNumber(materialId, targetVersion)
                .orElseThrow(() -> new ResourceNotFoundException("Версия " + targetVersion + " на материала не е намерена."));

        return mapToVersionResponse(materialVersion);
    }

    @Transactional(readOnly = true)
    public Material getMaterialEntity(UUID materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Материалът с ID " + materialId + " не е намерен."));
    }

    private boolean canViewHidden(Course course) {
        if (currentUserService.isAdmin()) {
            return true;
        }
        Optional<AuthenticatedUser> authUser = currentUserService.getAuthenticatedUser();
        if (authUser.isPresent() && authUser.get().getRole() == Role.TEACHER) {
            return course.getTeacher().getId().equals(authUser.get().getId());
        }
        return false;
    }

    private Set<Tag> resolveTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>();
        for (String rawName : tagNames) {
            if (rawName == null || rawName.isBlank()) continue;
            String normalized = rawName.trim().toLowerCase();
            Tag tag = tagRepository.findByName(normalized)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(normalized).build()));
            tags.add(tag);
        }
        return tags;
    }

    public MaterialResponse mapToResponse(Material material) {
        List<String> tags = material.getTags() != null
                ? material.getTags().stream().map(Tag::getName).sorted().toList()
                : Collections.emptyList();

        Course course = material.getLesson() != null && material.getLesson().getModule() != null
                ? material.getLesson().getModule().getCourse() : null;

        String fileUrl = null;
        if (material.getVersions() != null && !material.getVersions().isEmpty()) {
            fileUrl = material.getVersions().get(0).getFileUrl();
        }

        return MaterialResponse.builder()
                .id(material.getId())
                .lessonId(material.getLesson() != null ? material.getLesson().getId() : null)
                .courseOfferingId(course != null ? course.getExternalCourseOfferingId() : null)
                .courseCode(course != null ? course.getCode() : null)
                .type(material.getType())
                .title(material.getTitle())
                .description(material.getDescription())
                .tags(tags)
                .currentVersion(material.getCurrentVersion())
                .fileUrl(fileUrl)
                .videoUrl(material.getVideoUrl())
                .hidden(material.isHidden())
                .updatedAt(material.getUpdatedAt())
                .build();
    }

    public MaterialVersionResponse mapToVersionResponse(MaterialVersion version) {
        return MaterialVersionResponse.builder()
                .id(version.getId())
                .materialId(version.getMaterial() != null ? version.getMaterial().getId() : null)
                .versionNumber(version.getVersionNumber())
                .fileUrl(version.getFileUrl())
                .fileName(version.getFileName())
                .mimeType(version.getMimeType())
                .fileSizeBytes(version.getFileSizeBytes())
                .fileHash(version.getFileHash())
                .uploadedById(version.getUploadedBy() != null ? version.getUploadedBy().getId() : null)
                .uploadedByName(version.getUploadedBy() != null ? version.getUploadedBy().getFullName() : null)
                .uploadedAt(version.getUploadedAt())
                .build();
    }
}
