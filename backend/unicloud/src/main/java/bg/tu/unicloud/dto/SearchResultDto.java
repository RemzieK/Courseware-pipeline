package bg.tu.unicloud.dto;

import bg.tu.unicloud.model.MaterialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private String itemType; // "COURSE" or "MATERIAL"
    private UUID id;
    private String title;
    private String description;
    private String courseCode;
    private String discipline;
    private String teacherName;
    private MaterialType materialType;
}
