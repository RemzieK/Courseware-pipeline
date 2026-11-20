package bg.tu.unicloud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressResponse {
    private UUID courseId;
    private String courseCode;
    private int totalMaterials;
    private int readMaterials;
    private double progressPercentage;
    @Builder.Default
    private List<ReadMarkResponse> readMarks = new ArrayList<>();
}
