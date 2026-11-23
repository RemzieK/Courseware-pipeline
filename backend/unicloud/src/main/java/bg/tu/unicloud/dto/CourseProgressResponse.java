package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Напредък на студента по даден курс (брой прочетени материали)")
public class CourseProgressResponse {

    @Schema(description = "Идентификатор на курса", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID courseId;

    @Schema(description = "Код на курса", example = "CS-101")
    private String courseCode;

    @Schema(description = "Общ брой материали в курса", example = "24")
    private int totalMaterials;

    @Schema(description = "Брой прочетени материали от текущия студент", example = "10")
    private int readMaterials;

    @Schema(description = "Процент на завършеност (0–100)", example = "41.67")
    private double progressPercentage;

    @Schema(description = "Детайлни отметки за прочетените материали")
    @Builder.Default
    private List<ReadMarkResponse> readMarks = new ArrayList<>();
}
