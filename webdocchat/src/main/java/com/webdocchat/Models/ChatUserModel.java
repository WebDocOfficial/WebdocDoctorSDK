package com.webdocchat.Models;

/**
 * Created by WaleedPCC on 9/26/2019.
 */

public class ChatUserModel {

    private String userName;
    private String userID;
    private String firebaseID;
    private String status;
    private String lastSeen;
    private String appName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }


    /*public String getName() {
        return userName;
    }

    public void setName(String doctorName) {
        userName = doctorName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String doctorEmail) {
        Email = doctorEmail;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String doctorStatus) {
        Status = doctorStatus;
    }

    public String getFirebaseEmail() {
        return FirebaseEmail;
    }

    public void setFirebaseEmail(String doctorFirebaseEmail) {
        FirebaseEmail = doctorFirebaseEmail;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }*/
}
