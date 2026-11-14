package bg.tu.unicloud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Historical and physical file versions for learning materials.
 */
@Entity
@Table(
        name = "material_versions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_material_version", columnNames = {"material_id", "version_number"})
        },
        indexes = {
                @Index(name = "idx_material_versions_lookup", columnList = "material_id, version_number DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Min(1)
    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "file_url", length = 1000, nullable = false)
    private String fileUrl;

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "mime_type", length = 100, nullable = false)
    private String mimeType;

    @PositiveOrZero
    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Size(max = 64)
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialVersion version = (MaterialVersion) o;
        return id != null && id.equals(version.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

