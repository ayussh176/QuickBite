package com.quickbite.backend.delivery.controller;

import com.quickbite.backend.delivery.dto.GpsLocationPayload;
import com.quickbite.backend.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DeliveryLocationWebSocketController {

    private final DeliveryService deliveryService;

    @MessageMapping("/rider.location.{partnerId}")
    @SendTo("/topic/rider.location.{partnerId}")
    public GpsLocationPayload broadcastLocation(@DestinationVariable Long partnerId,
                                                 @Payload GpsLocationPayload payload) {
        log.debug("WebSocket coordinate ping for rider ID {}: ({}, {})", partnerId, payload.getLatitude(), payload.getLongitude());
        // Persist location coordinates to MySQL
        deliveryService.updateLocation(partnerId, payload.getLatitude(), payload.getLongitude());
        return payload;
    }
}
