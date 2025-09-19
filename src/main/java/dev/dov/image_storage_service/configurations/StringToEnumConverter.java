package dev.dov.image_storage_service.configurations;

import dev.dov.image_storage_service.image.enums.ImagesRequestType;
import org.springframework.core.convert.converter.Converter;

public class StringToEnumConverter implements Converter<String, ImagesRequestType> {

    @Override
    public ImagesRequestType convert(String source) {
        try {
            return ImagesRequestType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}

