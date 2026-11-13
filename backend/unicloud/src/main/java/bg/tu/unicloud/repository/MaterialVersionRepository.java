package bg.tu.unicloud.repository;

import bg.tu.unicloud.model.MaterialVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialVersionRepository extends JpaRepository<MaterialVersion, UUID> {

    List<MaterialVersion> findByMaterialIdOrderByVersionNumberDesc(UUID materialId);

    Optional<MaterialVersion> findByMaterialIdAndVersionNumber(UUID materialId, int versionNumber);
}

