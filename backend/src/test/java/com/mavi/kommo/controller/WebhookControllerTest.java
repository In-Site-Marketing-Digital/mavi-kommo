package com.mavi.kommo.controller;

import com.fasterxml.jackson.databind.node.NullNode;
import com.mavi.kommo.dto.FormPayload;
import com.mavi.kommo.service.LeadCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebhookControllerTest {

    private LeadCreationService leadCreationService;
    private WebhookController controller;

    @BeforeEach
    void setUp() {
        leadCreationService = mock(LeadCreationService.class);
        controller = new WebhookController(leadCreationService);

        when(leadCreationService.createLead(
                any(FormPayload.class),
                nullable(String.class)
        )).thenReturn(NullNode.getInstance());
    }

    @Test
    void queryDirectionHasPriorityOverPayloadDirection() {
        FormPayload payload = new FormPayload();
        payload.setDirection("insite");

        controller.receiveForm("mavi", payload);

        verify(leadCreationService).createLead(payload, "mavi");
    }

    @Test
    void topLevelPayloadDirectionIsUsedWhenQueryDirectionIsMissing() {
        FormPayload payload = new FormPayload();
        payload.setDirection("mavi");

        controller.receiveForm(null, payload);

        verify(leadCreationService).createLead(payload, "mavi");
    }

    @Test
    void fieldPayloadDirectionIsUsedWhenQueryAndTopLevelDirectionAreMissing() {
        FormPayload payload = new FormPayload();
        payload.setFields(Map.of("direction", "insite"));

        controller.receiveForm(null, payload);

        verify(leadCreationService).createLead(payload, "insite");
    }
}
