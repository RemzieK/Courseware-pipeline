package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Учебно занятие в рамките на модул")
public class LessonResponse {

    @Schema(description = "Уникален идентификатор на занятието", example = "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22")
    private UUID id;

    @Schema(description = "Идентификатор на модула, към който принадлежи занятието", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID moduleId;

    @Schema(description = "Заглавие на занятието", example = "Лекция 1 – Основни понятия")
    private String title;

    @Schema(description = "Описание на занятието")
    private String description;

    @Schema(description = "Позиция в модула (0-базирана)", example = "0")
    private int orderIndex;

    @Schema(description = "Дата и час на създаване (UTC)", example = "2026-09-07T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Дата и час на последна промяна (UTC)", example = "2026-11-15T08:30:00Z")
    private Instant updatedAt;
}
