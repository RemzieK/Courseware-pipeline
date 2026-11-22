package bg.tu.unicloud.dto;

import bg.tu.unicloud.model.MaterialType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данни за създаване на нов учебен материал")
public class MaterialCreateRequest {

    @NotNull(message = "Типът на материала е задължителен.")
    @Schema(description = "Тип на материала", example = "NOTES",
            allowableValues = {"NOTES", "PRESENTATION", "VIDEO_LINK", "ASSIGNMENT", "LITERATURE"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private MaterialType type;

    @NotBlank(message = "Заглавието на материала е задължително.")
    @Size(max = 255, message = "Заглавието не може да надвишава 255 символа.")
    @Schema(description = "Заглавие на материала (макс. 255 символа)", example = "Лекционни бележки – Седмица 1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Незадължително описание на материала")
    private String description;

    @Size(max = 1000, message = "URL адресът на видеото не може да надвишава 1000 символа.")
    @Schema(description = "URL на видео; задължителен при type=VIDEO_LINK", example = "https://www.youtube.com/watch?v=example")
    private String videoUrl;

    @Schema(description = "Тагове / теми за класифициране на материала", example = "[\"алгоритми\", \"сортиране\"]")
    private List<String> tags;

    @Schema(description = "Ако true, материалът е невидим за студенти", example = "false")
    private Boolean hidden;

    @Schema(description = "URL на начален файл (при JSON качване без multipart)", example = "https://storage.example.com/file.pdf")
    private String fileUrl;

    @Schema(description = "Оригинално наименование на файла", example = "lecture1.pdf")
    private String fileName;

    @Schema(description = "MIME тип на файла", example = "application/pdf")
    private String mimeType;

    @Schema(description = "Размер на файла в байтове", example = "204800")
    private Long fileSizeBytes;

    @Schema(description = "SHA-256 хеш на файла (за проверка на целостта)", example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
    private String fileHash;
}
