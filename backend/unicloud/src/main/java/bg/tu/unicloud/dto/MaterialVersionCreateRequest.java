package bg.tu.unicloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данни за качване на нова версия на файл чрез JSON (URL-базирано съхранение)")
public class MaterialVersionCreateRequest {

    @NotBlank(message = "URL адресът на файла е задължителен.")
    @Size(max = 1000, message = "URL адресът не може да надвишава 1000 символа.")
    @Schema(description = "URL на вече качения файл (макс. 1000 символа)", example = "https://storage.example.com/files/lecture1-v2.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;

    @NotBlank(message = "Името на файла е задължително.")
    @Size(max = 255, message = "Името на файла не може да надвишава 255 символа.")
    @Schema(description = "Оригинално наименование на файла (макс. 255 символа)", example = "lecture1-v2.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @NotBlank(message = "MIME типът е задължителен.")
    @Size(max = 100, message = "MIME типът не може да надвишава 100 символа.")
    @Schema(description = "MIME тип на файла (макс. 100 символа)", example = "application/pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mimeType;

    @PositiveOrZero(message = "Размерът на файла не може да бъде отрицателно число.")
    @Schema(description = "Размер на файла в байтове", example = "204800")
    private long fileSizeBytes;

    @Size(max = 64, message = "Хешът на файла не може да надвишава 64 символа.")
    @Schema(description = "SHA-256 хеш на файла (опционален, за проверка на целостта)", example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    private String fileHash;
}
