package eu.meterpoint.producer.api;

import tools.jackson.databind.JsonNode;

import java.util.UUID;

public record Envelope (
        UUID messageId,
        Action action,
        JsonNode payload
) {}
