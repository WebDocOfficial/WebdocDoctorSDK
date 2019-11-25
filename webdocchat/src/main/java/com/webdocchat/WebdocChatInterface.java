package com.webdocchat;

import java.util.List;

/**
 * Created by WaleedPCC on 10/22/2019.
 */

public interface WebdocChatInterface {

    public void getMessagesResponse(List<MessageDataModel> msgList);

    public void onUserStatusChangedResponse(String response);

    public void onChangeUserStatusResponse(String status, String lastSeen);

    public void onMessageSentResponse(String Response);

    public void isUserLoggedInResponse(Boolean response);
}
