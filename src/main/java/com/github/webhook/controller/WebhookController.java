package com.github.webhook.controller;

import com.github.webhook.service.CommitDataService;
import com.github.webhook.util.WebhookUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final CommitDataService commitDataService;

    public WebhookController(CommitDataService commitDataService) {
        this.commitDataService = commitDataService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {
        if (isInvalidSignature(payload, signature)) {
            logger.error("Invalid or missing signature");
            return new ResponseEntity<>("Invalid signature", HttpStatus.FORBIDDEN);
        }

        try {
            processPayload(payload);
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return new ResponseEntity<>("Error processing webhook", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Webhook received", HttpStatus.OK);
    }

    private boolean isInvalidSignature(String payload, String signature) {
        return signature == null || !WebhookUtils.isValidSignature(payload, signature);
    }

    private void processPayload(String payload) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(payload);
        commitDataService.processCommitData(jsonNode);
    }
}
