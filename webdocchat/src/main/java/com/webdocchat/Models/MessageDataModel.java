package com.webdocchat.Models;

/**
 * Created by WaleedPCC on 9/30/2019.
 */

public class MessageDataModel {

    private String sender;
    private String receiver;
    private String Message;
    private String MessageStatus;
    private long timestamp;
    private String type;
    private String MessageID;


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getMessageStatus() {
        return MessageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        MessageStatus = messageStatus;
    }

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageID() { return MessageID; }

    public void setMessageID(String messageID) { MessageID = messageID; }
}
