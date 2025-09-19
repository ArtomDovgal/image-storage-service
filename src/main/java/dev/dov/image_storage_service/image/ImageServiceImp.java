package dev.dov.image_storage_service.image;

import dev.dov.image_storage_service.image.interfaces.ImageService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class ImageServiceImp implements ImageService {

    @Value("${garbage.bucket.name}")
    private String bucketName;

    private final MinioClient minioClient;

    public ImageServiceImp(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

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
    public void addImage(String filename, InputStream inputStream) {

        try(BufferedInputStream bis = new BufferedInputStream(inputStream)) {

            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                    .bucket(bucketName)
                            .object(filename)
                            .stream(bis, bis.available(), 5 * 1024 * 1024)
                            .build());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void updateImage(String filename, InputStream inputStream) {

    }

    @Override
    @SneakyThrows(Exception.class)
    public String getPresignedObjectUrl(String filename) {

        ensureBucketExists();

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .method(Method.GET)
                        .build());
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

}
