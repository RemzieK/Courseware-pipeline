package bg.tu.unicloud.dto;

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
@Schema(description = "Тематичен модул (седмица) в курс")
public class ModuleResponse {

    @Schema(description = "Уникален идентификатор на модула", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID id;

    @Schema(description = "Идентификатор на курса, към който принадлежи модулът", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID courseId;

    @Schema(description = "Заглавие на модула", example = "Седмица 1 – Въведение")
    private String title;

    @Schema(description = "Описание на съдържанието на модула")
    private String description;

    @Schema(description = "Позиция на модула в курса (0-базирана)", example = "0")
    private int orderIndex;

    @Schema(description = "Занятия в модула, наредени по orderIndex")
    @Builder.Default
    private List<LessonResponse> lessons = new ArrayList<>();

    @Schema(description = "Дата и час на създаване (UTC)", example = "2026-09-05T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Дата и час на последна промяна (UTC)", example = "2026-11-10T09:00:00Z")
    private Instant updatedAt;
}
