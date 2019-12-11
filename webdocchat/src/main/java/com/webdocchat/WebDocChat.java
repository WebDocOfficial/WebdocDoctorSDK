package com.webdocchat;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.webdocchat.Models.ChatUserModel;
import com.webdocchat.NotificationManager.APIService;
import com.webdocchat.NotificationManager.Client;
import com.webdocchat.NotificationManager.Data;
import com.webdocchat.NotificationManager.MyResponse;
import com.webdocchat.NotificationManager.Sender;
import com.webdocchat.NotificationManager.Token;
import com.webdocchat.TimeAgo.TimeAgo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Waleed on 10/19/2019.
 */


public class WebDocChat {

    private static void uploadFile(final Context context, final FirebaseDatabase databaseReference, FirebaseStorage storageReference, final String senderAppName, final String receiverAppName, Uri fileUri, final String sender, final String receiver, final String type) {

        final Uri[] downloadUri = new Uri[1];
        StorageReference filePath = null;
        StorageTask uploadTask;

        if (fileUri != null) {
            //progressDialog.show();

            DatabaseReference ref = databaseReference.getReference().child("Messages");
            String messageID = ref.push().getKey();

            //String name = Environment.getExternalStorageDirectory().getAbsolutePath();

            switch (type) {
                case "image":
                    filePath = storageReference.getReference().child("Images/").child(messageID + ".jpg");
                    break;

                case "pdf":
                    filePath = storageReference.getReference().child("PDF Files/").child(messageID + "." + type);
                    break;

                case "docx":
                    filePath = storageReference.getReference().child("Docx Files/").child(messageID + "." + type);
                    break;
            }
            uploadTask = filePath.putFile(fileUri);
            final StorageReference finalFilePath = filePath;
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return finalFilePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        downloadUri[0] = task.getResult();
                        messageSend(context, databaseReference, senderAppName, receiverAppName, downloadUri[0].toString(), sender, receiver, type);
                    } else {
                        // Handle failures
                        //Toast.makeText(MessagesActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    public static void sendMessage(Context context, final String senderAppName, final String receiverAppName, Uri fileUri, final String sender, final String receiver, String msgType)
    {
        FirebaseApp appReference = firebaseAppReference(context);
        FirebaseDatabase databaseReference = FirebaseDatabase.getInstance(appReference);
        FirebaseStorage storageReference = FirebaseStorage.getInstance(appReference);

        uploadFile(context, databaseReference, storageReference, senderAppName, receiverAppName, fileUri, sender, receiver, msgType);
    }

    public static void sendMessage(Context context, final String senderAppName, final String receiverAppName, final String msg, final String sender, final String receiver, String msgType)
    {
        FirebaseApp appReference = firebaseAppReference(context);
        FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        messageSend(context, reference, senderAppName, receiverAppName, msg, sender, receiver, msgType);
    }

    public static void getMessage(final Context ctx, final String AppName, final String personalEmail, final String chatUserEmail) {

        FirebaseApp appReference = firebaseAppReference(ctx);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        final List<MessageDataModel> msgData = new ArrayList();
        DatabaseReference chatReference = reference.getReference();
        final WebdocChatInterface listener = (WebdocChatInterface) ctx;
        String chatKey = "";
        ArrayList TwoChattingUsersID = new ArrayList<>();
        TwoChattingUsersID.add(personalEmail);
        TwoChattingUsersID.add(chatUserEmail);
        Collections.sort(TwoChattingUsersID);
        chatKey = TwoChattingUsersID.get(0) + "_" + TwoChattingUsersID.get(1);
        final String finalChatKey = chatKey;
        chatReference.child("Messages").child(AppName).child(chatKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                msgData.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageDataModel msg = new MessageDataModel();
                    msg.setSender(snapshot.child("sender").getValue().toString());
                    msg.setReceiver(snapshot.child("receiver").getValue().toString());
                    msg.setMessage(snapshot.child("message").getValue().toString());
                    msg.setTimestamp(snapshot.child("timestamp").getValue().toString());
                    msg.setType(snapshot.child("type").getValue().toString());
                    msg.setMessageStatus(snapshot.child("MessageStatus").getValue().toString());
                    msgData.add(msg);
                }
                seenStatus(reference, AppName, personalEmail, chatUserEmail, finalChatKey);
                listener.getMessagesResponse(msgData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static String changeStatus(Context ctx, String AppName, String UserId, String status)
    {
        FirebaseApp appReference = firebaseAppReference(ctx);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        final WebdocChatInterface listener = (WebdocChatInterface) ctx;
        final String[] response = {""};

        DatabaseReference dbReference = reference.getReference().child("Users").child(AppName).child(UserId.replace(".",""));

        HashMap<String, Object> param = new HashMap<>();
        param.put("status", status);
        param.put("timestamp", ServerValue.TIMESTAMP);
        dbReference.updateChildren(param).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    response[0] = "success";
                    listener.onUserStatusChangedResponse(response[0]);
                } else {
                    response[0] = task.getException().getMessage().toString();
                    listener.onUserStatusChangedResponse(response[0]);
                }
            }
        });

        return response[0];
    }

    public static void checkStatus(final Context ctx, String AppName, String UserId) {

        FirebaseApp appReference = firebaseAppReference(ctx);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        final WebdocChatInterface listener = (WebdocChatInterface) ctx;

        DatabaseReference dbReference = reference.getReference("Users").child(AppName).child(UserId);
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = (String) dataSnapshot.child("status").getValue();
                long lastSeen = (long) dataSnapshot.child("timestamp").getValue();
                listener.onChangeUserStatusResponse(status, TimeAgo.getTimeAgo(lastSeen, ctx));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onChangeUserStatusResponse(databaseError.getMessage().toString(), "null");
            }
        });
    }

    private static void seenStatus(FirebaseDatabase reference, String AppName, final String personalEmail, final String chatUserEmail, String UsersChatKey) {

        final DatabaseReference changeMsgSeenStatusReference = reference.getReference().child("Messages").child(AppName).child(UsersChatKey);
        changeMsgSeenStatusReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageDataModel messages = snapshot.getValue(MessageDataModel.class);
                    if (messages.getReceiver().equals(personalEmail) && messages.getSender().equals(chatUserEmail)) {
                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put("MessageStatus", "seen");
                        changeMsgSeenStatusReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private static void sendNotification(final Context context, FirebaseDatabase reference, final String senderAppName, final String receiverAppName, final String sender, final String receiver, final String msg, final String msgType) {
        DatabaseReference tokens = reference.getReference("Tokens").child(receiverAppName);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                APIService apiService;
                Data data;
                apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Token token = snapshot.getValue(Token.class);

                    if(msgType.equalsIgnoreCase("image"))
                    {
                        data = new Data(sender, sender + ": " + "sent you a photo ", senderAppName, "Sent");
                    }
                    else
                    {
                        data = new Data(sender, sender + ": " + msg, senderAppName, "Sent");
                    }

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            //Toast.makeText(, response, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void updateToken(Context context, String AppName, String Userid) {

        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseDatabase dbreference = FirebaseDatabase.getInstance(appReference);

        DatabaseReference reference = dbreference.getReference("Tokens");
        Token token1 = new Token(FirebaseInstanceId.getInstance(appReference).getToken());
        reference.child(AppName).child(Userid).setValue(token1);
    }

    private static String messageSend(final Context context, final FirebaseDatabase reference, final String senderAppName, final String receiverAppName, final String msg, final String sender, final String receiver, final String msgType)
    {
        final boolean[] notify = {false};
        notify[0] = true;
        String UsersChatKey = "";
        final String[] response = {""};
        DatabaseReference chatReference = reference.getReference();
        String messageID = chatReference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("message", msg);
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("timestamp", ServerValue.TIMESTAMP);
        hashMap.put("messageID", messageID);
        hashMap.put("type", msgType);
        hashMap.put("MessageStatus", "sent");

        if(senderAppName.equalsIgnoreCase("PTCLHealth"))
        {
            HashMap<String, Object> chat_hashMap = new HashMap<String, Object>();
            chat_hashMap.put("chat", "true");
            chatReference.child("Chat").child(receiver).child(senderAppName).child(sender).updateChildren(chat_hashMap);
        }

        ArrayList TwoChattingUsersID = new ArrayList<>();
        TwoChattingUsersID.add(sender);
        TwoChattingUsersID.add(receiver);
        Collections.sort(TwoChattingUsersID);
        UsersChatKey = TwoChattingUsersID.get(0) + "_" + TwoChattingUsersID.get(1);

        chatReference.child("Messages").child("PTCLHealth").child(UsersChatKey).child(messageID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    response[0] = "success";
                    if (notify[0]) {
                        sendNotification(context, reference, senderAppName, receiverAppName, sender, receiver, msg, msgType);
                    }
                    notify[0] = false;
                } else {
                    response[0] = task.getException().getMessage();
                }
            }
        });

        return response[0];
    }

    public static void registerUserForChat(Context context, final String appName, final String name, final String email, final String password) {

        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);
        final FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance(appReference);

        final RegisterUserForChatInterface registerUserForChatInterface = (RegisterUserForChatInterface) context;
        final FirebaseAuth finalMAuth = mAuth;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //DatabaseReference firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(appName).child(email.replace(".", ""));
                            DatabaseReference firebaseDatabaseReference = reference.getReference("Users").child(appName).child(email.replace(".", ""));
                            HashMap<String, Object> hashMap = new HashMap();
                            hashMap.put("name", name);
                            hashMap.put("email", email);
                            hashMap.put("status", "online");
                            hashMap.put("timestamp", ServerValue.TIMESTAMP);

                            firebaseDatabaseReference.setValue(hashMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                registerUserForChatInterface.RegisterUserResponse("success");
                                            } else {
                                                registerUserForChatInterface.RegisterUserResponse(task.getException().toString());
                                            }
                                        }
                                    });
                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            finalMAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                registerUserForChatInterface.RegisterUserResponse("success");
                                            } else {
                                                registerUserForChatInterface.RegisterUserResponse(task.getException().getMessage());
                                            }
                                        }
                                    });
                        } else {
                            registerUserForChatInterface.RegisterUserResponse(task.getException().getMessage());
                        }
                    }
                });
    }


    public static void getChatUsersList(final Context context, final String email, String appName) {

        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        final WebdocChatUsersInterface vetDocChatUsersInterface = (WebdocChatUsersInterface) context;

        if ((appName.equalsIgnoreCase(String.valueOf(R.string.app_name))))
        {
            DatabaseReference chatUsersReference = reference.getReference().child("Users").child(String.valueOf(R.string.app_name));
            chatUsersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Global.ChatUsersList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatUserModel user = new ChatUserModel();

                        user.setFirebaseEmail(snapshot.getKey());
                        user.setName(snapshot.child("name").getValue().toString());
                        user.setEmail(snapshot.child("email").getValue().toString());
                        user.setStatus(snapshot.child("status").getValue().toString());
                        user.setAppName(String.valueOf(R.string.app_name));
                        Global.ChatUsersList.add(user);

                        vetDocChatUsersInterface.ChatUsers(Global.ChatUsersList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            final DatabaseReference dbReference = reference.getReference();

            dbReference.child("Chat").child(email.replace(".", "")).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        dbReference.child("Chat").child(email.replace(".", "")).child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                                Global.chatIDs.clear();

                                for (final DataSnapshot snapshot1 : dataSnapshot1.getChildren()) {

                                    Global.chatIDs.add(snapshot1.getKey());

                                    dbReference.child("Users").child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            Global.ChatUsersList.clear();

                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                ChatUserModel tempUser = new ChatUserModel();
                                                tempUser.setStatus(snapshot.child("status").getValue().toString());
                                                tempUser.setName(snapshot.child("name").getValue().toString());
                                                tempUser.setAppName(snapshot.getKey());
                                                tempUser.setFirebaseEmail(snapshot1.getKey());
                                                tempUser.setEmail(snapshot.child("email").getValue().toString());

                                                for(int i = 0; i < Global.chatIDs.size(); i++ )
                                                {
                                                    if(Global.chatIDs.get(i).equals(snapshot1.getKey()))
                                                    {
                                                        Global.ChatUsersList.add(tempUser);
                                                        vetDocChatUsersInterface.ChatUsers(Global.ChatUsersList);
                                                    }
                                                }

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }

    public static void isUserLoggedIn(Context context)
    {
        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);
        final FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance(appReference);

        final RegisterUserForChatInterface listener = (RegisterUserForChatInterface) context;

        if(mAuth.getCurrentUser() != null)
        {
            listener.isUserLoggedInResponse(true);
        }
        else
        {
            listener.isUserLoggedInResponse(false);
        }

    }

    public static void userLogout(Context context)
    {
        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance(appReference);

        mAuth.signOut();
    }

    private static FirebaseApp firebaseAppReference(Context context)
    {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyAWhpWnFmjGfkBEfLe2PfuypOYyGcH84LA")
                .setApplicationId("1:788347610980:android:cfea43ffde6fb4e25cfc71")
                .setDatabaseUrl("https://webdocdoctorsdk.firebaseio.com")
                .setStorageBucket("webdocdoctorsdk.appspot.com")
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
