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
@Schema(description = "Условие на курсова задача, предоставяно на Екип 3 (AutoJudge)")
public class AssignmentIntegrationResponse {

    @Schema(description = "Идентификатор на материала (задачата)", example = "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33")
    private UUID materialId;

    @Schema(description = "Идентификатор на офертата за курс", example = "1001")
    private Long courseOfferingId;

    @Schema(description = "Заглавие на задачата", example = "Задача 1 – Сортиране")
    private String title;

    @Schema(description = "Номер на текущата версия на файла с условието", example = "1")
    private int version;

    @Schema(description = "URL за изтегляне на условието", example = "https://storage.example.com/files/task1-v1.pdf")
    private String fileUrl;

    @Schema(description = "SHA-256 хеш на файла с условието", example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    private String sha256;

    @Schema(description = "Дата и час на последна актуализация (UTC)", example = "2026-11-01T09:00:00Z")
    private Instant updatedAt;
}
