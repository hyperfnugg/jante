package jante.mq;

public interface MessageQueueListener {
    void receiveMessages(MessageHandler handler);

    void requeueFailedMessages();

    int getErrorQueueSize();
}
