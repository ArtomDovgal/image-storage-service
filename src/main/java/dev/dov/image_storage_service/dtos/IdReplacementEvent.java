package dev.dov.image_storage_service.dtos;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class IdReplacementEvent {

    UUID correlationId;
    UUID newId;
    TypeOfImageReplacement typeOfImageReplacement;

}


