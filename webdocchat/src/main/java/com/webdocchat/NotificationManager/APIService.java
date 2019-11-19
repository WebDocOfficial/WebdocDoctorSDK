package com.webdocchat.NotificationManager;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Admin on 9/2/2019.
 */

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "AAAA-qur9_M:APA91bHaCWm11KjrdcARu9Jjo9_Mrllfavep7rMV1sRsJlh89TkpnKQ0mDUInNZq4DaAEvdvgXgpdBK1k3vkOVtXDi9-pyBCGVyI1LDCZG_uFMdlPjp62VmUhQ2szYrx150W7mJ-zqiF"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
