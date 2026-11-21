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
public class CourseDetailResponse {
    private UUID id;
    private String code;
    private Long externalCourseOfferingId;
    private String title;
    private String discipline;
    private String semester;
    private String description;
    private UUID teacherId;
    private String teacherName;
    private boolean archived;
    @Builder.Default
    private List<ModuleResponse> modules = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
