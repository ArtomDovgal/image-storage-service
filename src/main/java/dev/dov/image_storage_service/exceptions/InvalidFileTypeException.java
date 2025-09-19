package dev.dov.image_storage_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidFileTypeException  extends RuntimeException {

    public InvalidFileTypeException (String message) {
        super(message);
    }
}

