package bg.tu.unicloud.repository;

import bg.tu.unicloud.model.ReadMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReadMarkRepository extends JpaRepository<ReadMark, UUID> {

    Optional<ReadMark> findByUserIdAndMaterialId(UUID userId, UUID materialId);

    boolean existsByUserIdAndMaterialId(UUID userId, UUID materialId);

    List<ReadMark> findByUserId(UUID userId);

    @Query("SELECT rm FROM ReadMark rm " +
           "JOIN rm.material m " +
           "JOIN m.lesson l " +
           "JOIN l.module mod " +
           "JOIN mod.course c " +
           "WHERE rm.user.id = :userId AND c.id = :courseId")
    List<ReadMark> findByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    void deleteByUserIdAndMaterialId(UUID userId, UUID materialId);
}

