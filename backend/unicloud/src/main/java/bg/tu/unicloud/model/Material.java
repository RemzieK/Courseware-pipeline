package bg.tu.unicloud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a learning material (notes, presentation, video link, assignment, literature).
 */
@Entity
@Table(
        name = "materials",
        indexes = {
                @Index(name = "idx_materials_lesson_hidden", columnList = "lesson_id, is_hidden"),
                @Index(name = "idx_materials_type", columnList = "type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private MaterialType type;

    @NotBlank
    @Size(max = 255)
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 1000)
    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Builder.Default
    @Min(1)
    @Column(name = "current_version", nullable = false)
    private int currentVersion = 1;

    @Builder.Default
    @Column(name = "is_hidden", nullable = false)
    private boolean hidden = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("versionNumber DESC")
    @Builder.Default
    private List<MaterialVersion> versions = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "material_tags",
            joinColumns = @JoinColumn(name = "material_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReadMark> readMarks = new ArrayList<>();

    public void addVersion(MaterialVersion version) {
        versions.add(version);
        version.setMaterial(this);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getMaterials().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getMaterials().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        return id != null && id.equals(material.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

