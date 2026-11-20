package bg.tu.unicloud.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "integration_events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_integration_events_event_id", columnNames = "event_id")
        },
        indexes = {
                @Index(name = "idx_integration_events_retry", columnList = "status, next_retry_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @NotNull
    @Convert(converter = IntegrationEventTypeConverter.class)
    @Column(name = "event_type", length = 30, nullable = false)
    private IntegrationEventType eventType;

    @NotNull
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @NotBlank
    @Size(max = 255)
    @Column(name = "user_alias", length = 255, nullable = false)
    private String userAlias;

    @Column(name = "course_offering_id")
    private Long courseOfferingId;

    @Column(name = "material_id")
    private UUID materialId;

    @Min(1)
    @Column(name = "material_version")
    private Integer materialVersion;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private JsonNode payload;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Builder.Default
    @Min(0)
    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private IntegrationEventStatus status = IntegrationEventStatus.PENDING;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
}
