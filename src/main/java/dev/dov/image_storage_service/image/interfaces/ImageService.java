package dev.dov.image_storage_service.image.interfaces;

import java.io.File;
import java.io.InputStream;

public interface ImageService {

    File getImage(String filename);

    void deleteImage(String filename);

    void addImage(String filename, InputStream inputStream);

    void updateImage(String filename, InputStream inputStream);

    String getPresignedObjectUrl(String filename);

}
