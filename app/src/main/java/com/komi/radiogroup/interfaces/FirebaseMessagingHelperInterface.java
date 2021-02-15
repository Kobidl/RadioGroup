package com.komi.radiogroup.interfaces;

public interface FirebaseMessagingHelperInterface {

    void sendMessageToTopic(String fileUrl, String userId, String topic);

    void subscribeToTopic(String topicName);

    void unsubscribeFromTopic(String topicName);
}
