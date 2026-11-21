package bg.tu.unicloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {
    private UUID id;
    private UUID moduleId;
    private String title;
    private String description;
    private int orderIndex;
    private Instant createdAt;
    private Instant updatedAt;
}
