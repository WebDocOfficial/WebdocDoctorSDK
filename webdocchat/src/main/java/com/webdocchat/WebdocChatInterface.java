package com.webdocchat;

import com.webdocchat.Models.MessageDataModel;

import java.util.List;

/**
 * Created by WaleedPCC on 10/22/2019.
 */

public interface WebdocChatInterface {

    public void getMessagesResponse(List<MessageDataModel> msgList);

    public void onUserStatusCheckResponse(String status, String lastSeen);
}
