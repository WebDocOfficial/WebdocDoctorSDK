package com.webdocchat;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.webdocchat.Models.CallHistoryDataModel;
import com.webdocchat.Models.ChatUserModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WaleedPCC on 11/14/2019.
 */

public class Global {

    public static List<ChatUserModel> ChatUsersList = new ArrayList<>();
    public static List<CallHistoryDataModel> callHistoryData = new ArrayList<>();
    public static ArrayList chatIDs = new ArrayList<>();

    public static DatabaseReference seenReference;
    public static ValueEventListener seenListener;

}
