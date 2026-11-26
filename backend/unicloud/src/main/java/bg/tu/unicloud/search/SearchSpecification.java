package bg.tu.unicloud.search;

import bg.tu.unicloud.model.Course;
import bg.tu.unicloud.model.Lesson;
import bg.tu.unicloud.model.Material;
import bg.tu.unicloud.model.Module;
import bg.tu.unicloud.model.Tag;
import bg.tu.unicloud.model.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for the UC-31 full-text search feature.
 *
 * <p>
 * All fuzzy matching is delegated to the PostgreSQL {@code similarity()}
 * function
 * provided by the {@code pg_trgm} extension (enabled in V1__init_schema.sql).
 * The similarity threshold is set to 0.2 in V01__search_trgm.sql, which is
 * enough
 * to tolerate one mis-typed Cyrillic character in typical academic terms.
 * </p>
 *
 * <p>
 * No raw SQL strings appear here – matching predicates are built via
 * {@link CriteriaBuilder#function(String, Class, jakarta.persistence.criteria.Expression[])}
 * which lets Hibernate translate them into the correct PostgreSQL call.
 * </p>
 */
public final class SearchSpecification {

    /** Similarity threshold applied in every fuzzy predicate. */
    private static final double SIMILARITY_THRESHOLD = 0.2;

    private SearchSpecification() {
    }

    // -------------------------------------------------------------------------
    // Course specifications
    // -------------------------------------------------------------------------

    /**
     * Builds a {@link Specification} for {@link Course} that filters by keyword,
     * discipline and teacher name using pg_trgm similarity.
     *
     * @param q          optional free-text keyword (matches title, description,
     *                   code)
     * @param discipline optional discipline name (fuzzy match)
     * @param teacher    optional teacher full name (fuzzy match)
     */
    public static Specification<Course> courseMatches(String q, String discipline, String teacher) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only non-archived courses are searchable
            predicates.add(cb.isFalse(root.get("archived")));

            if (q != null) {
                predicates.add(cb.or(
                        similarityPredicate(cb, root.get("title"), q),
                        similarityPredicate(cb, root.get("description"), q),
                        // Course code uses LIKE (exact prefix / substring); codes are short
                        cb.like(cb.lower(root.get("code")), "%" + q.toLowerCase() + "%")));
            }

            if (discipline != null) {
                predicates.add(similarityPredicate(cb, root.get("discipline"), discipline));
            }

            if (teacher != null) {
                Join<Course, User> teacherJoin = root.join("teacher", JoinType.INNER);
                predicates.add(similarityPredicate(cb, teacherJoin.get("fullName"), teacher));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    // -------------------------------------------------------------------------
    // Material specifications
    // -------------------------------------------------------------------------

    /**
     * Builds a {@link Specification} for {@link Material} that filters by keyword,
     * discipline, teacher name, and topic (tag name) using pg_trgm similarity.
     *
     * <p>
     * The join path is:
     * {@code Material → Lesson → Module → Course → User(teacher)}.
     * When {@code topic} is provided, {@code Material → Tags} is also joined.
     * </p>
     *
     * @param q          optional free-text keyword (matches title, description)
     * @param discipline optional discipline name (fuzzy match against parent
     *                   course)
     * @param teacher    optional teacher full name (fuzzy match against course
     *                   teacher)
     * @param topic      optional topic / tag name (fuzzy match against material
     *                   tags)
     */
    public static Specification<Material> materialMatches(
            String q, String discipline, String teacher, String topic) {

        return (root, query, cb) -> {
            // distinct prevents duplicate rows when a material has multiple tags
            if (!Long.class.equals(query.getResultType())) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            // Hidden materials are not searchable
            predicates.add(cb.isFalse(root.get("hidden")));

            // Traverse the hierarchy to reach the parent Course and its teacher
            Join<Material, Lesson> lessonJoin = root.join("lesson", JoinType.INNER);
            Join<Lesson, Module> moduleJoin = lessonJoin.join("module", JoinType.INNER);
            Join<Module, Course> courseJoin = moduleJoin.join("course", JoinType.INNER);
            Join<Course, User> teacherJoin = courseJoin.join("teacher", JoinType.INNER);

            if (q != null) {
                predicates.add(cb.or(
                        similarityPredicate(cb, root.get("title"), q),
                        similarityPredicate(cb, root.get("description"), q)));
            }

            if (discipline != null) {
                predicates.add(similarityPredicate(cb, courseJoin.get("discipline"), discipline));
            }

            if (teacher != null) {
                predicates.add(similarityPredicate(cb, teacherJoin.get("fullName"), teacher));
            }

            if (topic != null) {
                Join<Material, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(similarityPredicate(cb, tagJoin.get("name"), topic));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Creates a predicate that evaluates
     * {@code similarity(field, term) >= SIMILARITY_THRESHOLD}
     * via the PostgreSQL pg_trgm function. Hibernate passes the call through to the
     * database
     * verbatim, so no raw SQL string is embedded here.
     */
    private static Predicate similarityPredicate(
            CriteriaBuilder cb,
            jakarta.persistence.criteria.Expression<String> field,
            String term) {

        return cb.greaterThanOrEqualTo(
                cb.function("similarity", Double.class, field, cb.literal(term)),
                SIMILARITY_THRESHOLD);
    }
}
