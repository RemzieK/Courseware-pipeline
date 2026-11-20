package bg.tu.unicloud.dto;

import bg.tu.unicloud.model.MaterialType;
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
public class MaterialCreateRequest {

    @NotNull(message = "Типът на материала е задължителен.")
    private MaterialType type;

    @NotBlank(message = "Заглавието на материала е задължително.")
    @Size(max = 255, message = "Заглавието не може да надвишава 255 символа.")
    private String title;

    private String description;

    @Size(max = 1000, message = "URL адресът на видеото не може да надвишава 1000 символа.")
    private String videoUrl;

    private List<String> tags;

    private Boolean hidden;

    // Optional initial version file metadata
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long fileSizeBytes;
    private String fileHash;
}
