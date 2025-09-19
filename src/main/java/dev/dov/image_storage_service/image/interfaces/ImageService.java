package dev.dov.image_storage_service.image.interfaces;

import dev.dov.image_storage_service.image.enums.ImagesRequestType;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public interface ImageService {

    File getImage(String filename);

    void deleteImage(String filename);

    void deleteImageByPrefix(String prefix);

    void addImage(String filename, InputStream inputStream,String contentType, boolean overwrite);

    void updateImage(String filename, InputStream inputStream);

    String getPresignedObjectUrl(String filename);

    void renameFilesByPrefix(String oldPrefix, String newPrefix);

    Map<String, String> getPresignedUrlsByPrefixAndType(String prefix, ImagesRequestType type);

}
