package bg.tu.unicloud.repository;

import bg.tu.unicloud.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);

    Page<Course> findByArchivedFalse(Pageable pageable);

    Page<Course> findByDisciplineAndSemesterAndArchivedFalse(String discipline, String semester, Pageable pageable);

    Page<Course> findByTeacherIdAndArchivedFalse(UUID teacherId, Pageable pageable);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.modules m LEFT JOIN FETCH m.lessons l WHERE c.id = :id")
    Optional<Course> findByIdWithStructure(@Param("id") UUID id);
}

