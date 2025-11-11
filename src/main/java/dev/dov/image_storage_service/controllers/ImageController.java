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
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(path = "/locations")
@AllArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/{location_id}/image/{image_id}/isValid", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> checkLocationImage(@PathVariable("location_id") String locationId,
                                                               @PathVariable("image_id") String imageId,
                                                               @RequestParam("file") MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("Must provide a valid image type");
        }

        String imageName = "loc-".concat(locationId).concat("_").concat(imageId);
        int status = imageService.checkImage(imageName, file,contentType,false);

        Map<String, Object> responseBody = new HashMap<>();

        return getResponseMessage(status, responseBody);

    }

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

        return getResponseMessage(status, responseBody);

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
        return getResponseMessage(status, responseBody);

    }


    @PostMapping(value = "/{location_id}/check/{check_id}/image/{image_id}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> addCheckImage(@PathVariable("location_id") String locationId,
                                                 @Path("check_id") String checkId,
                                                 @PathVariable("image_id") String imageId,
                                                 @RequestParam("file") MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("Must provide a valid image type");
        }

        String imageName = "check-".concat(locationId)
                .concat("_").concat(checkId)
                .concat("_").concat(imageId);

        int status = imageService.addImage(imageName, file,contentType, false);

        Map<String, Object> responseBody = new HashMap<>();
        return getResponseMessage(status, responseBody);

    }

    @PutMapping(value = "/{location_id}/check/{check_id}/image/{image_id}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> updateCheckImage(@PathVariable("location_id") String locationId,
                                                            @Path("check_id") String checkId,
                                                            @PathVariable("image_id") String imageId,
                                                            @RequestParam("file") MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("Must provide a valid image type");
        }

        String imageName = "check-".concat(locationId)
                .concat("_").concat(checkId)
                .concat("_").concat(imageId);

        int status = imageService.addImage(imageName, file,contentType, true);

        Map<String, Object> responseBody = new HashMap<>();
        return getResponseMessage(status, responseBody);

    }

    @GetMapping("/{location_id}/image/{image_id}/url")
    public ResponseEntity<String> getLocationImagePresignedObjectUrl(
                                        @PathVariable("location_id") String location_id,
                                        @PathVariable("image_id") String imageId) {

        String imageName = "loc-".concat(location_id).concat("_").concat(imageId);
        return ResponseEntity.ok().body(imageService.getPresignedObjectUrl(imageName));
    }

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

    @DeleteMapping("/{location_id}/image")
    public ResponseEntity<Void> deleteAllLocationImage(
            @PathVariable("location_id") String locationId) {

        String imageName = "loc-".concat(locationId).concat("_");
        imageService.deleteImage(imageName);
        return ResponseEntity.ok().build();
    }

    @NotNull
    private static ResponseEntity<Map<String, Object>> getResponseMessage(int status, Map<String, Object> responseBody) {
        String message;
        HttpStatus httpStatus;

        switch (status) {
            case 0 -> {
                message = "Зображення визначено як небезпечне";
                httpStatus = HttpStatus.FORBIDDEN;
            }
            case 1 -> {
                message = "Помилка при додаванні зображення";
                httpStatus = HttpStatus.BAD_REQUEST;
            }
            case 2 -> {
                message = "Зображення збережено успішно";
                httpStatus = HttpStatus.OK;
            }
            default -> throw new IllegalStateException("Unexpected value: " + status);
        }

        responseBody.put("status", status);
        responseBody.put("message", message);

        return ResponseEntity
                .status(httpStatus)
                .body(responseBody);
    }


}
