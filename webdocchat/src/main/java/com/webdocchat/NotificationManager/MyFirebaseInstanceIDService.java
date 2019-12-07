package com.webdocchat.NotificationManager;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Nabeel on 9/6/2018.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        FirebaseApp appReference = firebaseAppReference(getApplicationContext());
        final FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance(appReference);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        String refreshToken = FirebaseInstanceId.getInstance(appReference).getToken();

        if (firebaseUser != null) {
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {

        FirebaseApp appReference = firebaseAppReference(getApplicationContext());
        final FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance(appReference);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance(appReference).getReference("Tokens");

        Token token = new Token(refreshToken);

        reference.child(firebaseUser.getUid()).setValue(token);
    }


    private static FirebaseApp firebaseAppReference(Context context)
    {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyAWhpWnFmjGfkBEfLe2PfuypOYyGcH84LA")
                .setApplicationId("1:788347610980:android:cfea43ffde6fb4e25cfc71")
                .setDatabaseUrl("https://webdocdoctorsdk.firebaseio.com")
                .setStorageBucket("webdocdoctorsdk.appspot.com")
                .setGcmSenderId("788347610980")
                .build();

        try {
            FirebaseApp app = FirebaseApp.initializeApp(context, options, "WebDocDoctorSDK");
            return app;
        }
        catch (IllegalStateException e)
        {
            return FirebaseApp.getInstance("WebDocDoctorSDK");
        }
    }
}
