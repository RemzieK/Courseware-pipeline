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
public class AssignmentIntegrationResponse {
    private UUID materialId;
    private Long courseOfferingId;
    private String title;
    private int version;
    private String fileUrl;
    private String sha256;
    private Instant updatedAt;
}
