package bg.tu.unicloud.dto;

import bg.tu.unicloud.model.MaterialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponse {
    private UUID id;
    private UUID lessonId;
    private Long courseOfferingId;
    private String courseCode;
    private MaterialType type;
    private String title;
    private String description;
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    private int currentVersion;
    private String fileUrl;
    private String videoUrl;
    private boolean hidden;
    private Instant updatedAt;
}
