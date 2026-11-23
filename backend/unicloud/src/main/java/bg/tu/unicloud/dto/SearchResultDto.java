package bg.tu.unicloud.dto;

import bg.tu.unicloud.model.MaterialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Единичен резултат от търсене – може да бъде курс или учебен материал")
public class SearchResultDto {

    @Schema(description = "Тип на намерения ресурс", example = "COURSE", allowableValues = {"COURSE", "MATERIAL"})
    private String itemType;

    @Schema(description = "Уникален идентификатор на ресурса", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Заглавие на курса или материала", example = "Въведение в програмирането")
    private String title;

    @Schema(description = "Описание на курса или материала")
    private String description;

    @Schema(description = "Код на свързания курс", example = "CS-101")
    private String courseCode;

    @Schema(description = "Академична дисциплина на курса", example = "Информатика")
    private String discipline;

    @Schema(description = "Пълно име на преподавателя", example = "доц. д-р Иван Петров")
    private String teacherName;

    @Schema(description = "Тип на материала; null когато itemType е COURSE",
            example = "NOTES", allowableValues = {"NOTES", "PRESENTATION", "VIDEO_LINK", "ASSIGNMENT", "LITERATURE"})
    private MaterialType materialType;
}
