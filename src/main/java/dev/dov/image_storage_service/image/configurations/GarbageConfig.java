package dev.dov.image_storage_service.image.configurations;

import dev.dov.image_storage_service.image.interfaces.ImageService;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GarbageConfig {

    @Value("${garbage.url}")
    private String url;

    @Value("${garbage.access-key}")
    private String accessKey;

    @Value("${garbage.secret-key}")
    private String secretKey;

    @Bean(name = "garbageMinioClient")
    public MinioClient garbageConfig() {

        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

}
