package bg.tu.unicloud.service;

import bg.tu.unicloud.dto.PagedResponse;
import bg.tu.unicloud.dto.SearchResultDto;
import bg.tu.unicloud.model.Course;
import bg.tu.unicloud.model.Material;
import bg.tu.unicloud.repository.CourseRepository;
import bg.tu.unicloud.repository.MaterialRepository;
import bg.tu.unicloud.search.SearchSpecification;
import bg.tu.unicloud.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

        /**
         * Executes a fuzzy search across courses and materials using pg_trgm
         * similarity.
         *
         * <p>
         * All filter parameters are optional. When {@code q} is provided, the search
         * uses the PostgreSQL {@code similarity()} function (threshold 0.2) so that
         * single-character Cyrillic typos are tolerated. The {@code teacher} and
         * {@code topic} parameters were previously ignored; they are now fully wired
         * through {@link SearchSpecification} to the database.
         * </p>
         *
         * @param q          free-text keyword; matched against title, description, and
         *                   course code
         * @param discipline discipline name filter (fuzzy)
         * @param teacher    teacher full name filter (fuzzy)
         * @param topic      tag / topic name filter (fuzzy, matched against material
         *                   tags)
         * @param page       zero-based page number
         * @param size       page size (1–100)
         * @param sort       sort expression, e.g. {@code "title,asc"}
         */
        @Transactional(readOnly = true)
        public PagedResponse<SearchResultDto> search(String q, String discipline, String teacher, String topic,
                        Integer page, Integer size, String sort) {
                Pageable pageable = PaginationUtils.createPageable(
                                page, size, sort, ALLOWED_SORT_FIELDS, "id", Sort.Direction.ASC);

                String queryTerm = normalise(q);
                String discTerm = normalise(discipline);
                String teacherTerm = normalise(teacher);
                String topicTerm = normalise(topic);

                log.debug("Search query: q={}, discipline={}, teacher={}, topic={}", queryTerm, discTerm, teacherTerm,
                                topicTerm);

                Specification<Course> courseSpec = SearchSpecification.courseMatches(queryTerm, discTerm, teacherTerm);
                Specification<Material> materialSpec = SearchSpecification.materialMatches(queryTerm, discTerm,
                                teacherTerm, topicTerm);

                Page<Course> coursePage = courseRepository.findAll(courseSpec, pageable);
                Page<Material> materialPage = materialRepository.findAll(materialSpec, pageable);

                List<SearchResultDto> results = new ArrayList<>(
                                coursePage.getNumberOfElements() + materialPage.getNumberOfElements());

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
                                        ? m.getLesson().getModule().getCourse()
                                        : null;
                        results.add(SearchResultDto.builder()
                                        .itemType("MATERIAL")
                                        .id(m.getId())
                                        .title(m.getTitle())
                                        .description(m.getDescription())
                                        .courseCode(c != null ? c.getCode() : null)
                                        .discipline(c != null ? c.getDiscipline() : null)
                                        .teacherName(c != null && c.getTeacher() != null ? c.getTeacher().getFullName()
                                                        : null)
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

        /**
         * Trims and returns null for blank strings so the specification skips the
         * predicate.
         */
        private static String normalise(String value) {
                return (value != null && !value.isBlank()) ? value.trim() : null;
        }
}
