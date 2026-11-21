package bg.tu.unicloud.dto;

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
public class MaterialUpdateRequest {

    @Size(max = 255, message = "Заглавието не може да надвишава 255 символа.")
    private String title;

    private String description;

    @Size(max = 1000, message = "URL адресът на видеото не може да надвишава 1000 символа.")
    private String videoUrl;

    private List<String> tags;

    private Boolean hidden;
}
