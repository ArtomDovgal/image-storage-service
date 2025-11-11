package dev.dov.image_storage_service.controllers;

import dev.dov.image_storage_service.exceptions.InvalidFileTypeException;
import dev.dov.image_storage_service.image.enums.ImagesRequestType;
import dev.dov.image_storage_service.image.interfaces.ImageService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
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
@RequestMapping(path = "/users/image")
@AllArgsConstructor
public class UserController {

    private final ImageService imageService;

    @PostMapping(path = "/{username}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> addUserImage(@RequestParam("file") MultipartFile file,
                                                           @PathVariable("username") String username) {

        //TODO
        String userId = username;

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("Must provide a valid image type");
        }

        String imageName = "user-".concat(userId);
        int status = imageService.addImage(imageName, file,contentType,true);

        Map<String, Object> responseBody = new HashMap<>();
        String message = getMessage(status);
        responseBody.put("status", status);
        responseBody.put("message", message);

        return ResponseEntity.ok(responseBody);

    }

    @PutMapping(path = "/{username}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,Object>> updateUserImage(@RequestParam("file") MultipartFile file,
                                                           @PathVariable("username") String username) {

        //TODO
        String userId = username;

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("Must provide a valid image type");
        }

        String imageName = "user-".concat(userId);
        int status = imageService.addImage(imageName, file,contentType,true);

        Map<String, Object> responseBody = new HashMap<>();
        String message = getMessage(status);
        responseBody.put("status", status);
        responseBody.put("message", message);

        return ResponseEntity.ok(responseBody);

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

        //TODO.
        String user_id = username;

        String imageName = "user-".concat(user_id);
        String url = imageService.getPresignedObjectUrl(imageName);
        return ResponseEntity.ok().body(url);
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
