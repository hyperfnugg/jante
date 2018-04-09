package jante.mq;

import com.fasterxml.jackson.databind.JsonNode;

public interface MessageHandler {
    void handle(JsonNode message);
}
