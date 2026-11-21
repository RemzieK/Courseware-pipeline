package bg.tu.unicloud.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile store(MultipartFile file, String subDirectory);
    Resource loadAsResource(String storagePath);
    void delete(String storagePath);
}
