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
@Schema(description = "Отметка „прочетено" за учебен материал от конкретен студент")
public class ReadMarkResponse {

    @Schema(description = "Уникален идентификатор на отметката", example = "e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55")
    private UUID id;

    @Schema(description = "Идентификатор на студента (собственик на отметката)", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    private UUID userId;

    @Schema(description = "Идентификатор на прочетения материал", example = "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33")
    private UUID materialId;

    @Schema(description = "Версия на материала, при която е поставена отметката", example = "1")
    private int versionAtRead;

    @Schema(description = "Флаг дали е публикувана нова версия след прочитане", example = "false")
    private boolean hasNewVersion;

    @Schema(description = "Дата и час на отбелязване като прочетен (UTC)", example = "2026-11-20T18:45:00Z")
    private Instant readAt;
}
