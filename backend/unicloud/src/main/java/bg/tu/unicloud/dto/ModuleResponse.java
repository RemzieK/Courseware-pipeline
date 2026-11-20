package bg.tu.unicloud.dto;

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
public class ModuleResponse {
    private UUID id;
    private UUID courseId;
    private String title;
    private String description;
    private int orderIndex;
    @Builder.Default
    private List<LessonResponse> lessons = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
