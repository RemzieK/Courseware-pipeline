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
    List<Material> findByCourseCodeAndType(@Param("courseCode") String courseCode, @Param("type") MaterialType type);

    @Query("SELECT m FROM Material m " +
           "LEFT JOIN FETCH m.versions v " +
           "LEFT JOIN FETCH m.tags t " +
           "WHERE m.id = :id")
    Optional<Material> findByIdWithDetails(@Param("id") UUID id);
}

