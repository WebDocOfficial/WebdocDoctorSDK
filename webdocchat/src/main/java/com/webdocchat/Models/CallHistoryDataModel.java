package com.webdocchat.Models;

/**
 * Created by WaleedPCC on 10/5/2019.
 */

public class CallHistoryDataModel {

    private String callHistoryId;
    private String callType;
    private String establishedTime;
    private String callerId;
    private String doctorId;
    private String isMissedCall;
    private long timeStamp;

    public String getCallHistoryId() {
        return callHistoryId;
    }

    public void setCallHistoryId(String callHistoryId) {
        this.callHistoryId = callHistoryId;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getEstablishedTime() {
        return establishedTime;
    }

    public void setEstablishedTime(String establishedTime) { this.establishedTime = establishedTime; }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getIsMissedCall() {
        return isMissedCall;
    }

    public void setIsMissedCall(String isMissedCall) {
        this.isMissedCall = isMissedCall;
    }

    public long getTimeStamp() { return timeStamp; }

    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }
}
