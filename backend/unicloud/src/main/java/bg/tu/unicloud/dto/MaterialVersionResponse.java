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
@Schema(description = "Конкретна файлова версия на учебен материал")
public class MaterialVersionResponse {

    @Schema(description = "Уникален идентификатор на версията", example = "d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44")
    private UUID id;

    @Schema(description = "Идентификатор на материала", example = "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33")
    private UUID materialId;

    @Schema(description = "Номер на версията (1-базиран, нараства при всяко качване)", example = "2")
    private int versionNumber;

    @Schema(description = "URL за изтегляне на файла", example = "https://storage.example.com/files/lecture1-v2.pdf")
    private String fileUrl;

    @Schema(description = "Оригинално наименование на файла", example = "lecture1-v2.pdf")
    private String fileName;

    @Schema(description = "MIME тип на файла", example = "application/pdf")
    private String mimeType;

    @Schema(description = "Размер на файла в байтове", example = "204800")
    private long fileSizeBytes;

    @Schema(description = "SHA-256 хеш на файла", example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    private String fileHash;

    @Schema(description = "Идентификатор на потребителя, качил версията", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    private UUID uploadedById;

    @Schema(description = "Пълно име на потребителя, качил версията", example = "доц. д-р Иван Петров")
    private String uploadedByName;

    @Schema(description = "Дата и час на качване (UTC)", example = "2026-11-15T10:30:00Z")
    private Instant uploadedAt;
}
