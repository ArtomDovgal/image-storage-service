package dev.dov.image_storage_service.controllers;

import dev.dov.image_storage_service.exceptions.InvalidFileTypeException;
import dev.dov.image_storage_service.image.interfaces.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(path = "/users/image")
@AllArgsConstructor
public class UserController {

    private final ImageService imageService;

    @PostMapping(path = "/{username}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addUserImage(@RequestParam("file") MultipartFile file,
                                             @PathVariable("username") String username) {

        //TODO
        String userId = username;

        try {

            String contentType = file.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidFileTypeException("Must provide a valid image type");
            }

            String imageName = "user-".concat(userId);
            imageService.addImage(imageName, file.getInputStream(),contentType,true);

            return ResponseEntity.ok().build();

        } catch (IOException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping(path = "/{username}")
    public ResponseEntity<Void> deleteUserImage(@PathVariable("username") String username) {

        //TODO
        String user_id = username;

        String imageName = "user-".concat(user_id);
        imageService.deleteImage(imageName);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/{username}")
    public ResponseEntity<String> getUserImagePresignedObjectUrl(@PathVariable("username") String username) {

        //TODO
        String user_id = username;

        String imageName = "user-".concat("_").concat(user_id);
        return ResponseEntity.ok().body(imageName);
    }
}
