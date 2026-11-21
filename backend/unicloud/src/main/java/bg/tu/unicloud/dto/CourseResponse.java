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
public class CourseResponse {
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
    private Instant createdAt;
    private Instant updatedAt;
}
