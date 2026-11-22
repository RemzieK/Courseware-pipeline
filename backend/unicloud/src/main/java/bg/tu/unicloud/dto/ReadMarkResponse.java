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
public class ReadMarkResponse {
    private UUID id;
    private UUID userId;
    private UUID materialId;
    private int versionAtRead;
    private boolean hasNewVersion;
    private Instant readAt;
}
