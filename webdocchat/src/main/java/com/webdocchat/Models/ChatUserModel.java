package com.webdocchat.Models;

/**
 * Created by WaleedPCC on 9/26/2019.
 */

public class ChatUserModel {

    private String name;
    private String email;
    private String status;
    private String firebaseEmail;
    private String appName;
    private String unreadMessageCounter;
    private String lastMessage;
    private String lastMessageTimestamp;
    private String lastMessageType;
    private String lastMessageSender;
    private String lastMessageReceiver;
    private String lastMessageStatus;
    private String lastMessageTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFirebaseEmail() {
        return firebaseEmail;
    }

    public void setFirebaseEmail(String firebaseEmail) {
        this.firebaseEmail = firebaseEmail;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUnreadMessageCounter() { return unreadMessageCounter; }

    public void setUnreadMessageCounter(String unreadMessageCounter) { this.unreadMessageCounter = unreadMessageCounter; }

    public String getLastMessage() { return lastMessage; }

    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastMessageType() { return lastMessageType; }

    public void setLastMessageType(String lastMessageType) { this.lastMessageType = lastMessageType; }

    public String getLastMessageTimestamp() { return lastMessageTimestamp; }

    public void setLastMessageTimestamp(String lastMessageTimestamp) { this.lastMessageTimestamp = lastMessageTimestamp; }

    public String getLastMessageSender() { return lastMessageSender; }

    public void setLastMessageSender(String lastMessageSender) { this.lastMessageSender = lastMessageSender; }

    public String getLastMessageReceiver() { return lastMessageReceiver; }

    public void setLastMessageReceiver(String lastMessageReceiver) { this.lastMessageReceiver = lastMessageReceiver; }

    public String getLastMessageStatus() { return lastMessageStatus; }

    public void setLastMessageStatus(String lastMessageStatus) { this.lastMessageStatus = lastMessageStatus; }

    public String getLastMessageTime() { return lastMessageTime; }

    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }
}
