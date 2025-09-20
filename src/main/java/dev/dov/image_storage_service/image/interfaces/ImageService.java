package dev.dov.image_storage_service.image.interfaces;

import dev.dov.image_storage_service.image.enums.ImagesRequestType;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ImageService {

    File getImage(String filename);

    void deleteImage(String filename);

    void deleteImageByPrefix(String prefix);

    void addImage(String filename, InputStream inputStream,String contentType);

    void updateImage(String filename, InputStream inputStream);

    String getPresignedObjectUrl(String filename);

    void renameFilesByPrefix(String oldPrefix, String newPrefix);

    Map<String, String> getPresignedUrlsByPrefixAndType(String prefix, ImagesRequestType type);

}
