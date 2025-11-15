package com.adtech.clickaggregator.infrastructure.adapter.in.rest;

import com.adtech.clickaggregator.domain.model.ClickEvent;
import com.adtech.clickaggregator.domain.port.in.ProcessClickEventUseCase;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.dto.ClickEventRequest;
import com.adtech.clickaggregator.infrastructure.adapter.in.rest.mapper.ClickEventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ClickEventController
 */
@WebMvcTest(ClickEventController.class)
class ClickEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcessClickEventUseCase processClickEventUseCase;

    @MockBean
    private ClickEventMapper clickEventMapper;

    @Test
    void testRecordClick_Success() throws Exception {
        ClickEventRequest request = ClickEventRequest.builder()
                .adId("ad-123")
                .campaignId("campaign-456")
                .userId("user-789")
                .country("US")
                .deviceType("mobile")
                .cost(0.25)
                .build();

        ClickEvent mockEvent = ClickEvent.builder()
                .eventId("event-123")
                .adId("ad-123")
                .campaignId("campaign-456")
                .timestamp(Instant.now())
                .cost(0.25)
                .build();

        when(clickEventMapper.toDomain(any())).thenReturn(mockEvent);
        when(processClickEventUseCase.processClick(any())).thenReturn(mockEvent);
        when(clickEventMapper.toResponse(any())).thenCallRealMethod();

        mockMvc.perform(post("/api/v1/clicks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.event_id").value("event-123"))
                .andExpect(jsonPath("$.ad_id").value("ad-123"))
                .andExpect(jsonPath("$.campaign_id").value("campaign-456"));
    }

    @Test
    void testRecordClick_MissingAdId() throws Exception {
        ClickEventRequest request = ClickEventRequest.builder()
                .campaignId("campaign-456")
                .cost(0.25)
                .build();

        mockMvc.perform(post("/api/v1/clicks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/v1/clicks/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
