package bg.tu.unicloud.dto;

import bg.tu.unicloud.model.MaterialType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Учебен материал с метаданни и текуща файлова версия")
public class MaterialResponse {

    @Schema(description = "Уникален идентификатор на материала", example = "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33")
    private UUID id;

    @Schema(description = "Идентификатор на занятието, към което принадлежи материалът", example = "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22")
    private UUID lessonId;

    @Schema(description = "Идентификатор на офертата за курс (за интеграции с Екип 3)", example = "1001")
    private Long courseOfferingId;

    @Schema(description = "Код на курса, към чийто модул принадлежи материалът", example = "CS-101")
    private String courseCode;

    @Schema(description = "Тип на материала", example = "NOTES",
            allowableValues = {"NOTES", "PRESENTATION", "VIDEO_LINK", "ASSIGNMENT", "LITERATURE"})
    private MaterialType type;

    @Schema(description = "Заглавие на материала", example = "Лекционни бележки – Седмица 1")
    private String title;

    @Schema(description = "Описание на съдържанието на материала")
    private String description;

    @Schema(description = "Тагове / теми за класифициране на материала", example = "[\"алгоритми\", \"сортиране\"]")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Schema(description = "Номер на текущата (най-нова) версия на файла", example = "2")
    private int currentVersion;

    @Schema(description = "URL за изтегляне/стрийминг на файла; null при VIDEO_LINK")
    private String fileUrl;

    @Schema(description = "URL на видео (YouTube, Moodle и др.); попълнен само при VIDEO_LINK", example = "https://www.youtube.com/watch?v=example")
    private String videoUrl;

    @Schema(description = "Флаг дали материалът е скрит от студенти", example = "false")
    private boolean hidden;

    @Schema(description = "Дата и час на последна промяна (UTC)", example = "2026-11-20T14:00:00Z")
    private Instant updatedAt;
}
