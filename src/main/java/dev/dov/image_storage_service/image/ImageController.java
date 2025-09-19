package dev.dov.image_storage_service.image;

import dev.dov.image_storage_service.exceptions.InvalidFileTypeException;
import dev.dov.image_storage_service.image.interfaces.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController("/locations")
@AllArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/{location_id}/image/{image_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addImage(@PathVariable("location_id") String problemId,
                                         @PathVariable("image_id") String imageId,
                                         @RequestParam("file") MultipartFile file) {

        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidFileTypeException("Must provide a valid image type");
            }
            String imageName = problemId.concat("_").concat(imageId);
            imageService.addImage(imageName, file.getInputStream(),contentType);
            return ResponseEntity.ok().build();

        } catch (IOException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{location_id}/image/{image_id}/url")
    public String getPresignedObjectUrl(
                                        @PathVariable("location_id") String location_id,
                                        @PathVariable("image_id") String imageId) {

        String imageName = location_id.concat("_").concat(imageId);
        return imageService.getPresignedObjectUrl(imageName);
    }

    @DeleteMapping("/{location_id}/image/{image_id}")
    public ResponseEntity<Void> deleteProblemImage(
            @PathVariable("location_id") Long problemId,
            @PathVariable("image_id") Long imageId) {

        String imageName = String.valueOf(problemId).concat("_").concat(String.valueOf(imageId));
        imageService.deleteImage(imageName);
        return ResponseEntity.ok().build();
    }

}
