package bg.tu.unicloud.dto;

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
public class MaterialVersionCreateRequest {

    @NotBlank(message = "URL адресът на файла е задължителен.")
    @Size(max = 1000, message = "URL адресът не може да надвишава 1000 символа.")
    private String fileUrl;

    @NotBlank(message = "Името на файла е задължително.")
    @Size(max = 255, message = "Името на файла не може да надвишава 255 символа.")
    private String fileName;

    @NotBlank(message = "MIME типът е задължителен.")
    @Size(max = 100, message = "MIME типът не може да надвишава 100 символа.")
    private String mimeType;

    @PositiveOrZero(message = "Размерът на файла не може да бъде отрицателно число.")
    private long fileSizeBytes;

    @Size(max = 64, message = "Хешът на файла не може да надвишава 64 символа.")
    private String fileHash;
}
