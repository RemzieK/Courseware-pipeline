package bg.tu.unicloud.service;

import bg.tu.unicloud.dto.PagedResponse;
import bg.tu.unicloud.dto.SearchResultDto;
import bg.tu.unicloud.model.Course;
import bg.tu.unicloud.model.Material;
import bg.tu.unicloud.repository.CourseRepository;
import bg.tu.unicloud.repository.MaterialRepository;
import bg.tu.unicloud.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final CourseRepository courseRepository;
    private final MaterialRepository materialRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "title", "createdAt");

    @Transactional(readOnly = true)
    public PagedResponse<SearchResultDto> search(String q, String discipline, String teacher, String topic,
                                                Integer page, Integer size, String sort) {
        Pageable pageable = PaginationUtils.createPageable(
                page, size, sort, ALLOWED_SORT_FIELDS, "id", Sort.Direction.ASC
        );

        String queryTerm = (q != null && !q.isBlank()) ? q.trim() : null;
        String discTerm = (discipline != null && !discipline.isBlank()) ? discipline.trim() : null;

        Page<Course> coursePage = courseRepository.searchCourses(queryTerm, discTerm, pageable);
        Page<Material> materialPage = materialRepository.searchMaterials(queryTerm, discTerm, pageable);

        List<SearchResultDto> results = new ArrayList<>();
        for (Course c : coursePage.getContent()) {
            results.add(SearchResultDto.builder()
                    .itemType("COURSE")
                    .id(c.getId())
                    .title(c.getTitle())
                    .description(c.getDescription())
                    .courseCode(c.getCode())
                    .discipline(c.getDiscipline())
                    .teacherName(c.getTeacher() != null ? c.getTeacher().getFullName() : null)
                    .build());
        }

        for (Material m : materialPage.getContent()) {
            Course c = m.getLesson() != null && m.getLesson().getModule() != null
                    ? m.getLesson().getModule().getCourse() : null;
            results.add(SearchResultDto.builder()
                    .itemType("MATERIAL")
                    .id(m.getId())
                    .title(m.getTitle())
                    .description(m.getDescription())
                    .courseCode(c != null ? c.getCode() : null)
                    .discipline(c != null ? c.getDiscipline() : null)
                    .teacherName(c != null && c.getTeacher() != null ? c.getTeacher().getFullName() : null)
                    .materialType(m.getType())
                    .build());
        }

        long total = coursePage.getTotalElements() + materialPage.getTotalElements();

        return PagedResponse.<SearchResultDto>builder()
                .content(results)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(total)
                .build();
    }
}
