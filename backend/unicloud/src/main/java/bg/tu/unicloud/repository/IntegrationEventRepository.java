package bg.tu.unicloud.repository;

import bg.tu.unicloud.model.IntegrationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface IntegrationEventRepository extends JpaRepository<IntegrationEvent, UUID> {

    @Query("""
            SELECT event
            FROM IntegrationEvent event
            WHERE event.status = bg.tu.unicloud.model.IntegrationEventStatus.PENDING
              AND event.retryCount < 3
              AND (event.nextRetryAt IS NULL OR event.nextRetryAt <= :now)
            ORDER BY event.occurredAt
            """)
    Page<IntegrationEvent> findDispatchableEvents(
            @Param("now") Instant now,
            Pageable pageable
    );
}
