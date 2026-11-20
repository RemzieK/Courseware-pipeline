package bg.tu.unicloud.dto;

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
public class MaterialVersionResponse {
    private UUID id;
    private UUID materialId;
    private int versionNumber;
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private long fileSizeBytes;
    private String fileHash;
    private UUID uploadedById;
    private String uploadedByName;
    private Instant uploadedAt;
}
