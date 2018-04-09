package jante.mq;

import com.fasterxml.jackson.databind.JsonNode;

public interface MessageQueueSender {
    void queueMessage(JsonNode message);
}
