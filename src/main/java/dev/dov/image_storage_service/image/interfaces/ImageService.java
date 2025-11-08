package dev.dov.image_storage_service.image.interfaces;

import dev.dov.image_storage_service.image.enums.ImagesRequestType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public interface ImageService {

    File getImage(String filename);

    void deleteImage(String filename);

    void deleteImageByPrefix(String prefix);

    int addImage(String filename, MultipartFile file, String contentType, boolean overwrite);

    void updateImage(String filename, InputStream inputStream);

    String getPresignedObjectUrl(String filename);

    void renameFilesByPrefix(String oldPrefix, String newPrefix);

    Map<String, String> getPresignedUrlsByPrefixAndType(String prefix, ImagesRequestType type);

}
