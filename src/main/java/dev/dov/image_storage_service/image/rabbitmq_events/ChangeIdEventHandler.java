package dev.dov.image_storage_service.image.rabbitmq_events;

import dev.dov.image_storage_service.dtos.IdReplacementEvent;
import dev.dov.image_storage_service.image.interfaces.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ChangeIdEventHandler {

    private final ImageService imageService;

    @RabbitListener(queues = "${amqp.exchange.id-replace.queue}")
    void handleMultiplicationSolved(final IdReplacementEvent event) {
        log.info("New change id Event received: {}", event.getCorrelationId());
        try {
            imageService.renameFilesByPrefix(event.getCorrelationId().toString(),event.getNewId().toString());
        } catch (final Exception e) {
            log.error("Error when trying to process IdReplacementEvent", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }


}
