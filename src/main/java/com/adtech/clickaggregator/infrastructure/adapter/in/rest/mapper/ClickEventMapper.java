package com.adtech.clickaggregator.infrastructure.adapter.in.rest.mapper;

import com.adtech.clickaggregator.domain.model.ClickEvent;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto.ClickEventRequest;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto.ClickEventResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper between ClickEvent domain model and REST DTOs
 */
@Component
public class ClickEventMapper {

    public ClickEvent toDomain(ClickEventRequest request) {
        return ClickEvent.create(
                request.getAdId(),
                request.getCampaignId(),
                request.getUserId(),
                request.getIpAddress(),
                request.getUserAgent(),
                request.getCountry(),
                request.getDeviceType(),
                request.getCost()
        );
    }

    public ClickEventResponse toResponse(ClickEvent event) {
        return ClickEventResponse.builder()
                .eventId(event.getEventId())
                .adId(event.getAdId())
                .campaignId(event.getCampaignId())
                .timestamp(event.getTimestamp())
                .status("ACCEPTED")
                .message("Click event received successfully")
                .build();
    }

    public ClickEventResponse toErrorResponse(String message) {
        return ClickEventResponse.builder()
                .status("ERROR")
                .message(message)
                .build();
    }
}
