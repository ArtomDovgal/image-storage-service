package dev.dov.image_storage_service.services_extensions.nsfw;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class NsfwImageChecker {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${nsfw.server.url}")
    private String nsfwApiUrl;

    public boolean isNsfw(MultipartFile file) throws IOException {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

        var request = new HttpEntity<>(body, headers);
        var response = restTemplate.postForEntity(nsfwApiUrl, request, Map.class);

        return (Boolean) response.getBody().get("is_nsfw");
    }
}
