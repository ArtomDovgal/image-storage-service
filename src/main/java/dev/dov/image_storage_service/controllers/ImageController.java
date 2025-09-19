package dev.dov.image_storage_service.controllers;

import dev.dov.image_storage_service.exceptions.InvalidFileTypeException;
import dev.dov.image_storage_service.image.enums.ImagesRequestType;
import dev.dov.image_storage_service.image.interfaces.ImageService;
import lombok.AllArgsConstructor;
import org.simpleframework.xml.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping(path = "/locations")
@AllArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/{location_id}/image/{image_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addLocationImage(@PathVariable("location_id") String locationId,
                                         @PathVariable("image_id") String imageId,
                                         @RequestParam("file") MultipartFile file) {

        try {

            String contentType = file.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidFileTypeException("Must provide a valid image type");
            }

            String imageName = "loc-".concat(locationId).concat("_").concat(imageId);
            imageService.addImage(imageName, file.getInputStream(),contentType,false);

            return ResponseEntity.ok().build();

        } catch (IOException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/{location_id}/check/{check_id}/image/{image_id}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addCheckImage(@PathVariable("location_id") String locationId,
                                                 @Path("check_id") String checkId,
                                                 @PathVariable("image_id") String imageId,
                                                 @RequestParam("file") MultipartFile file) {

        try {

            String contentType = file.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidFileTypeException("Must provide a valid image type");
            }

            String imageName = "check-".concat(locationId)
                    .concat("_").concat(checkId)
                    .concat("_").concat(imageId);

            imageService.addImage(imageName, file.getInputStream(),contentType, false);

            return ResponseEntity.ok().build();

        } catch (IOException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

}
