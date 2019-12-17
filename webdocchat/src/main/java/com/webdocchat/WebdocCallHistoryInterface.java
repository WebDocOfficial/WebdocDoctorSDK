package com.webdocchat;

import com.webdocchat.Models.CallHistoryDataModel;
import com.webdocchat.Models.MessageDataModel;

import java.util.List;

/**
 * Created by Admin on 12/17/2019.
 */

public interface WebdocCallHistoryInterface {

    public void getCallHistoryResponse(List<CallHistoryDataModel> callHistoryList);
}
