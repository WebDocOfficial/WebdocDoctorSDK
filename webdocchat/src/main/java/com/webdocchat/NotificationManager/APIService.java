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
                    "AAAAt40t52Q:APA91bFgDBSXgDCmbo5EVteyQM68-_5oyXg47wXk-Bp7KyriiBSoqurCCYqLNkWEJcJogaj0vUYiNJmZl2rDebRZ8wmyxhjqJsBpy-bmWG0NXp3gWJswRLEXUxfRnEuHez7vfHe37QCU"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
