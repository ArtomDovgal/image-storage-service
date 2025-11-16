package dev.dov.image_storage_service.controllers;

import dev.dov.image_storage_service.exceptions.InvalidFileTypeException;
import dev.dov.image_storage_service.image.enums.ImagesRequestType;
import dev.dov.image_storage_service.image.interfaces.ImageService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.simpleframework.xml.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping(path = "/locations")
@AllArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/{location_id}/image/{image_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> addLocationImage(@PathVariable("location_id") String locationId,
                                         @PathVariable("image_id") String imageId,
                                         @RequestParam("file") MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("Must provide a valid image type");
        }

        String imageName = "loc-".concat(locationId).concat("_").concat(imageId);
        int status = imageService.addImage(imageName, file,contentType,false);

        Map<String, Object> responseBody = new HashMap<>();
        String message = getMessage(status);
        responseBody.put("status", status);
        responseBody.put("message", message);

        return ResponseEntity.ok(responseBody);
    }

    @PutMapping(value = "/{location_id}/image/{image_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> updateLocationImage(@PathVariable("location_id") String locationId,
                                                               @PathVariable("image_id") String imageId,
                                                               @RequestParam("file") MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("Must provide a valid image type");
        }

        String imageName = "loc-".concat(locationId).concat("_").concat(imageId);
        int status = imageService.addImage(imageName, file,contentType,true);

        Map<String, Object> responseBody = new HashMap<>();
        String message = getMessage(status);
        responseBody.put("status", status);
        responseBody.put("message", message);

        return ResponseEntity.ok(responseBody);

    }


    @PostMapping(value = "/{location_id}/check/image/{check_image_id}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> addCheckImage(@PathVariable("location_id") String locationId,
                                                 @PathVariable("check_image_id") String checkImageId,
                                                 @RequestParam("file") MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("Must provide a valid image type");
        }

        String imageName = "check-".concat(locationId)
                .concat("_").concat(checkImageId)
                .concat("_").concat(String.valueOf(UUID.randomUUID()));

        int status = imageService.addImage(imageName, file,contentType, false);

        Map<String, Object> responseBody = new HashMap<>();
        String message = getMessage(status);
        responseBody.put("status", status);
        responseBody.put("message", message);

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping(
            value = "/{location_id}/check/image/{check_image_id}/all",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> addCheckImages(
            @PathVariable("location_id") String locationId,
            @PathVariable("check_image_id") String checkImageId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new InvalidFileTypeException("No images provided");
        }

        List<String> storedImageNames = new ArrayList<>();

        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidFileTypeException("Must provide valid image files");
            }

            String imageName = "check-".concat(locationId)
                    .concat("_").concat(checkImageId)
                    .concat("_").concat(String.valueOf(UUID.randomUUID()));

            int status = imageService.addImage(imageName, file, contentType, false);

            if (status != 0) {
                storedImageNames.add(imageName);
            }
        }

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "OK");
        responseBody.put("uploadedCount", storedImageNames.size());
        responseBody.put("imageNames", storedImageNames);

        return ResponseEntity.ok(responseBody);
    }


    @PutMapping(value = "/{location_id}/check/{check_image_id}/image/{image_id}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> updateCheckImage(@PathVariable("location_id") String locationId,
                                                            @Path("check_image_id") String checkImageId,
                                                            @PathVariable("image_id") String imageId,
                                                            @RequestParam("file") MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("Must provide a valid image type");
        }

        String imageName = "check-".concat(locationId)
                .concat("_").concat(checkImageId)
                .concat("_").concat(imageId);

        int status = imageService.addImage(imageName, file,contentType, true);

        Map<String, Object> responseBody = new HashMap<>();
        String message = getMessage(status);
        responseBody.put("status", status);
        responseBody.put("message", message);

        return ResponseEntity.ok(responseBody);

    }

    @GetMapping("/{location_id}/image/{image_id}/url")
    public ResponseEntity<String> getLocationImagePresignedObjectUrl(
                                        @PathVariable("location_id") String location_id,
                                        @PathVariable("image_id") String imageId) {

        String imageName = "loc-".concat(location_id).concat("_").concat(imageId);
        return ResponseEntity.ok().body(imageService.getPresignedObjectUrl(imageName));
    }

    //for check send (location_id)_(check_image_id),type = check
    @GetMapping("/{location_id}/image/url")
    public ResponseEntity<Map<String,String>> getLocationImagePresignedObjectUrls(
            @PathVariable("location_id") String location_id,
            @RequestParam("type") ImagesRequestType type) {

        return ResponseEntity.ok(imageService.getPresignedUrlsByPrefixAndType(location_id, type));
    }

    @DeleteMapping("/{location_id}/image/{image_id}")
    public ResponseEntity<Void> deleteLocationImage(
            @PathVariable("location_id") String locationId,
            @PathVariable("image_id") String imageId) {

        String imageName = "loc-".concat(locationId).concat("_").concat(imageId);
        imageService.deleteImage(imageName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{location_id}/check/{check_image_id}/image/{image_id}")
    public ResponseEntity<Void> deleteCheckImage(
            @PathVariable("location_id") String locationId,
            @PathVariable("check_image_id") String checkImageId,
            @PathVariable("image_id") String imageId) {

        String imageName = "check-".concat(locationId)
                .concat("_").concat(checkImageId)
                .concat("_").concat(imageId);

        imageService.deleteImage(imageName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{location_id}/image")
    public ResponseEntity<Void> deleteAllLocationImage(
            @PathVariable("location_id") String locationId) {

        String imageName = "loc-".concat(locationId).concat("_");
        imageService.deleteImage(imageName);
        return ResponseEntity.ok().build();
    }

    @NotNull
    private static String getMessage(int status) {
        return switch (status) {
            case 0 -> "Зображення визначено як небезпечне";
            case 1 -> "Помилка при додаванні зображення";
            case 2 -> "Зображення збережено успішно";
            default -> throw new IllegalStateException("Unexpected value: " + status);
        };
    }

}
