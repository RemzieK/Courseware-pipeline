package bg.tu.unicloud.repository;

import bg.tu.unicloud.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {

    Optional<Course> findByCode(String code);

    Optional<Course> findByExternalCourseOfferingId(Long externalCourseOfferingId);

    boolean existsByCode(String code);

    Page<Course> findByArchivedFalse(Pageable pageable);

    Page<Course> findByDisciplineAndSemesterAndArchivedFalse(String discipline, String semester, Pageable pageable);

    Page<Course> findByTeacherIdAndArchivedFalse(UUID teacherId, Pageable pageable);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.modules m LEFT JOIN FETCH m.lessons l WHERE c.id = :id")
    Optional<Course> findByIdWithStructure(@Param("id") UUID id);

    
    @Query("""
            SELECT c FROM Course c
            WHERE (:archived IS NULL OR c.archived = :archived)
              AND (:discipline IS NULL OR LOWER(c.discipline) = :discipline)
              AND (:semester IS NULL OR LOWER(c.semester) = :semester)
              AND (:teacherId IS NULL OR c.teacher.id = :teacherId)
            """)
    Page<Course> findWithFilters(
            @Param("discipline") String discipline,
            @Param("semester") String semester,
            @Param("teacherId") UUID teacherId,
            @Param("archived") Boolean archived,
            Pageable pageable
    );
}
