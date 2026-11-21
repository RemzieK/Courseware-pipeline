package bg.tu.unicloud.service;

import bg.tu.unicloud.dto.AssignmentIntegrationResponse;
import bg.tu.unicloud.dto.PagedResponse;
import bg.tu.unicloud.model.Course;
import bg.tu.unicloud.model.Material;
import bg.tu.unicloud.model.MaterialVersion;
import bg.tu.unicloud.repository.MaterialRepository;
import bg.tu.unicloud.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final MaterialRepository materialRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "updatedAt", "createdAt");

    @Transactional(readOnly = true)
    public PagedResponse<AssignmentIntegrationResponse> getAssignments(Long courseOfferingId, Integer page, Integer size, String sort) {
        Pageable pageable = PaginationUtils.createPageable(
                page, size, sort, ALLOWED_SORT_FIELDS, "updatedAt", Sort.Direction.DESC
        );

        Page<Material> materials = materialRepository.findAssignmentsWithFilters(courseOfferingId, pageable);

        List<AssignmentIntegrationResponse> dtos = materials.getContent().stream()
                .map(this::mapToIntegrationResponse)
                .toList();

        return PagedResponse.<AssignmentIntegrationResponse>builder()
                .content(dtos)
                .page(materials.getNumber())
                .size(materials.getSize())
                .totalElements(materials.getTotalElements())
                .build();
    }

    private AssignmentIntegrationResponse mapToIntegrationResponse(Material m) {
        Course course = m.getLesson() != null && m.getLesson().getModule() != null
                ? m.getLesson().getModule().getCourse() : null;

        String fileUrl = null;
        String sha256 = null;

        if (m.getVersions() != null && !m.getVersions().isEmpty()) {
            MaterialVersion latest = m.getVersions().get(0);
            fileUrl = latest.getFileUrl();
            sha256 = latest.getFileHash();
        }

        return AssignmentIntegrationResponse.builder()
                .materialId(m.getId())
                .courseOfferingId(course != null ? course.getExternalCourseOfferingId() : null)
                .title(m.getTitle())
                .version(m.getCurrentVersion())
                .fileUrl(fileUrl)
                .sha256(sha256)
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
