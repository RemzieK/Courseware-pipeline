package bg.tu.unicloud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks student read/completion progress on materials.
 */
@Entity
@Table(
        name = "read_marks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_material_read", columnNames = {"user_id", "material_id"})
        },
        indexes = {
                @Index(name = "idx_read_marks_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadMark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Builder.Default
    @Min(1)
    @Column(name = "version_at_read", nullable = false)
    private int versionAtRead = 1;

    @CreationTimestamp
    @Column(name = "read_at", nullable = false)
    private Instant readAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReadMark readMark = (ReadMark) o;
        return id != null && id.equals(readMark.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

