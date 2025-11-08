package dev.dov.image_storage_service.image;

import dev.dov.image_storage_service.image.enums.ImagesRequestType;
import dev.dov.image_storage_service.image.interfaces.ImageService;
import dev.dov.image_storage_service.services_extensions.nsfw.NsfwImageChecker;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageServiceImp implements ImageService {

    @Value("${garbage.bucket-name}")
    private String bucketName;

//    @Value("${garage.public.url}")
//    private String publicUrl;

    private final MinioClient minioClient;

    private final NsfwImageChecker nsfwImageChecker;


    @Override
    @SneakyThrows(Exception.class)
    public File getImage(String filename) {

        try {
            minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @SneakyThrows(Exception.class)
    public void deleteImage(String filename) {

        ensureBucketExists();

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build());

    }

    @Override
    public int addImage(String filename, MultipartFile file, String contentType, boolean overwrite) {

        try {
           boolean imageIsNsfw = nsfwImageChecker.isNsfw(file);

            if(imageIsNsfw) {
                return 0;
            }
        } catch (IOException e) {
                e.printStackTrace();
        }

        try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream())) {

            ensureBucketExists();

            if (!overwrite && isSuchImageAlreadyExist(filename)) {
                throw new IllegalStateException("Зображення '" + filename + "' вже існує.");
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .contentType(contentType)
                            .stream(bis, bis.available(), 5 * 1024 * 1024)
                            .build()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 2;
    }

    private boolean isSuchImageAlreadyExist(String filename) throws InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException, ErrorResponseException {

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );

            return true;

        } catch (io.minio.errors.ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new RuntimeException("Помилка при перевірці існування зображення: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new RuntimeException("Помилка при перевірці існування зображення: " + e.getMessage(), e);
        }
    }


    @Override
    public void updateImage(String filename, InputStream inputStream) {

    }

    @Override
    @SneakyThrows(Exception.class)
    public String getPresignedObjectUrl(String filename) {

        ensureBucketExists();

        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .method(Method.GET)
                        .build());

        //return rewritePresignedUrl(url);
        return url;
    }

    private void ensureBucketExists() throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        }
    }

    @Override
    @SneakyThrows
    public void renameFilesByPrefix(String oldPrefix, String newPrefix) {
        ensureBucketExists();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(oldPrefix)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            String oldName = item.objectName();
            String newName = oldName.replaceFirst("^" + oldPrefix, newPrefix);

            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder()
                                    .bucket(bucketName)
                                    .object(oldName)
                                    .build())
                            .bucket(bucketName)
                            .object(newName)
                            .build()
            );

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(oldName)
                            .build()
            );
        }
    }

    @Override
    @SneakyThrows(Exception.class)
    public Map<String, String> getPresignedUrlsByPrefixAndType(String locationId, ImagesRequestType type) {

        ensureBucketExists();

        Map<String, String> urlsMap = new HashMap<>();

        String imageName = "def";

        switch (type) {
            case LOCATION -> imageName = "loc-".concat(locationId).concat("_");
            case CHECK -> imageName = "check-".concat(locationId).concat("_");
        }

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(imageName)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            String objectName = item.objectName();
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .build()
            );
            String[] imageIdParts = objectName.split("_");

            switch (imageIdParts.length) {
                case 2 -> urlsMap.put(imageIdParts[imageIdParts.length-1], url);
                case 3 -> urlsMap.put(imageIdParts[1], url);
            }

        }

        return urlsMap;
    }

    @Override
    @SneakyThrows(Exception.class)
    public void deleteImageByPrefix(String prefix) {
        ensureBucketExists();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );

        List<DeleteObject> objectsToDelete = new ArrayList<>();

        for (Result<Item> result : results) {
            String objectName = result.get().objectName();
            objectsToDelete.add(new DeleteObject(objectName));
        }

        if (!objectsToDelete.isEmpty()) {
            minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objectsToDelete)
                            .build()
            );
        }
    }

//    public String rewritePresignedUrl(String internalUrl) {
//        return internalUrl.replace("http://garage:9000", publicUrl);
//    }
}
