package bg.tu.unicloud.repository;

import bg.tu.unicloud.model.Material;
import bg.tu.unicloud.model.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {

        List<Material> findByLessonIdAndHiddenFalse(UUID lessonId);

        List<Material> findByLessonId(UUID lessonId);

        List<Material> findByType(MaterialType type);

        @Query("SELECT m FROM Material m " +
                        "JOIN m.lesson l " +
                        "JOIN l.module mod " +
                        "JOIN mod.course c " +
                        "WHERE c.code = :courseCode AND m.type = :type AND m.hidden = false")
        List<Material> findByCourseCodeAndType(@Param("courseCode") String courseCode,
                        @Param("type") MaterialType type);

        @Query("SELECT m FROM Material m " +
                        "LEFT JOIN FETCH m.versions v " +
                        "LEFT JOIN FETCH m.tags t " +
                        "WHERE m.id = :id")
        Optional<Material> findByIdWithDetails(@Param("id") UUID id);

        @Query("""
                        SELECT m FROM Material m
                        JOIN m.lesson l
                        JOIN l.module mod
                        JOIN mod.course c
                        WHERE (:courseOfferingId IS NULL OR c.externalCourseOfferingId = :courseOfferingId)
                          AND m.type = bg.tu.unicloud.model.MaterialType.ASSIGNMENT
                          AND m.hidden = false
                        """)
        org.springframework.data.domain.Page<Material> findAssignmentsWithFilters(
                        @Param("courseOfferingId") Long courseOfferingId,
                        org.springframework.data.domain.Pageable pageable);

        @Query("""
                        SELECT m FROM Material m
                        JOIN m.lesson l
                        JOIN l.module mod
                        JOIN mod.course c
                        WHERE m.hidden = false
                          AND (:q IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%'))
                               OR LOWER(m.description) LIKE LOWER(CONCAT('%', :q, '%')))
                          AND (:discipline IS NULL OR LOWER(c.discipline) LIKE LOWER(CONCAT('%', :discipline, '%')))
                        """)
        org.springframework.data.domain.Page<Material> searchMaterials(
                        @Param("q") String q,
                        @Param("discipline") String discipline,
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT COUNT(m) FROM Material m " +
                        "JOIN m.lesson l " +
                        "JOIN l.module mod " +
                        "JOIN mod.course c " +
                        "WHERE c.id = :courseId AND m.hidden = false")
        long countNonHiddenMaterialsByCourseId(@Param("courseId") UUID courseId);
}
