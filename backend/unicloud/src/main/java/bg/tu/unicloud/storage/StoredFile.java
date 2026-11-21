package bg.tu.unicloud.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile {
    private String storagePath;
    private String fileName;
    private String mimeType;
    private long fileSizeBytes;
    private String fileHash;
}
