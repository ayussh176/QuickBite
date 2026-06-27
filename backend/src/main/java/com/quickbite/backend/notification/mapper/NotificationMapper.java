package com.quickbite.backend.notification.mapper;

import com.quickbite.backend.config.MapStructConfig;
import com.quickbite.backend.notification.dto.NotificationResponse;
import com.quickbite.backend.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
