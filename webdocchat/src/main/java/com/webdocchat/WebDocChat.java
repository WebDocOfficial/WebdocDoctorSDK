package com.webdocchat;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

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

    /*private static void FirebaseChatUsers(DatabaseReference reference, final ArrayList chatIDs)
    {
        reference.child("Users").child("PTCLHealth").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //GlobalNew.ListUsers.clear();

                int i = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String firebaseID = (String) snapshot.child("firebaseID").getValue();
                    String userID = (String) snapshot.child("userID").getValue();
                    String userName = (String) snapshot.child("userName").getValue();
                    String status = (String) snapshot.child("status").getValue();
                    Long lastSeen = (Long) snapshot.child("lastSeen").getValue();

                    User user = new User(firebaseID, userID, userName, status, lastSeen);
                    //Toast.makeText(_MainActivity.this, user.getUsername(), Toast.LENGTH_SHORT).show();
                    Log.e("NAME------",user.getUsername());

                    if(chatIDs.contains(snapshot.getKey()))
                    {
                        GlobalNew.ListUsers.add(user);
                        i++;
                    }

                    if (i == chatIDs.size())
                    {
                        if(!loadFirstTime)
                        {
                            loadFirstTime = true;
                            selectFirstItemAsDefault();
                            //GlobalNew.utils.dismissLoadingPopup();
                        }

                            *//* Updating adapter *//*
                        if(GlobalNew.isUserInChat)
                        {
                            ChatFragment.usersAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void firebaseUsersforDoctors(Context context, String doctorID)
    {
        final ArrayList chatIDs = new ArrayList();

        final WebdocChatUsersInterface webdocChatUsersInterface = (WebdocChatUsersInterface) context;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Chat").child(doctorID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.getChildrenCount() == 0)
                {
                    webdocChatUsersInterface.ChatUsers(null);
                    *//*loadFirstTime = true;
                    selectFirstItemAsDefault();*//*
                    //GlobalNew.utils.dismissLoadingPopup();
                }
                else {
                    //GlobalNew.ListUsers.clear();
                    //chatIDs.clear();

                    int i = 0;
                    int size = (int) dataSnapshot.getChildrenCount();

                    for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        chatIDs.add(snapshot.getKey());
                        i++;

                        if (i == size) {
                            FirebaseChatUsers(chatIDs);
                        }
                    }
                }

                *//*UsersAdapter usersAdapter = new UsersAdapter(ListUsers, true);
                userRecyclerView.setAdapter(usersAdapter);*//*
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    public static void uploadFile(final String senderAppName, final String receiverAppName, Uri fileUri, final String sender, final String receiver, final String type) {

        final Uri[] downloadUri = new Uri[1];
        StorageReference filePath = null;
        StorageTask uploadTask;

        if (fileUri != null) {
            //progressDialog.show();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Messages");
            String messageID = ref.push().getKey();

            //String name = Environment.getExternalStorageDirectory().getAbsolutePath();

            switch (type) {
                case "image":
                    filePath = FirebaseStorage.getInstance().getReference().child("Images/").child(messageID + ".jpg");
                    break;

                case "pdf":
                    filePath = FirebaseStorage.getInstance().getReference().child("PDF Files/").child(messageID + "." + type);
                    break;

                case "docx":
                    filePath = FirebaseStorage.getInstance().getReference().child("Docx Files/").child(messageID + "." + type);
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
                        messageSend(senderAppName, receiverAppName, downloadUri[0].toString(), sender, receiver, type);
                    } else {
                        // Handle failures
                        //Toast.makeText(MessagesActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    public static void sendMessage(final String senderAppName, final String receiverAppName, Uri fileUri, final String sender, final String receiver, String msgType) {
        uploadFile(senderAppName, receiverAppName, fileUri, sender, receiver, msgType);
    }

    public static void sendMessage(final String senderAppName, final String receiverAppName, final String msg, final String sender, final String receiver, String msgType) {
        messageSend(senderAppName, receiverAppName, msg, sender, receiver, msgType);
    }

    public static void getMessage(Context ctx, final String AppName, final String personalEmail, final String chatUserEmail) {
        final List<MessageDataModel> msgData = new ArrayList();
        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference();
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
                    msg.setTime(snapshot.child("timestamp").getValue().toString());
                    msg.setType(snapshot.child("type").getValue().toString());
                    msg.setMessageStatus(snapshot.child("MessageStatus").getValue().toString());
                    msgData.add(msg);
                }
                seenStatus(AppName, personalEmail, chatUserEmail, finalChatKey);
                listener.getMessagesResponse(msgData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static String changeStatus(Context ctx, String AppName, String UserId, String status) {
        final WebdocChatInterface listener = (WebdocChatInterface) ctx;
        final String[] response = {""};
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("Users").child(AppName).child(UserId);
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

    public static void checkStatus(Context ctx, String AppName, String UserId) {
        final WebdocChatInterface listener = (WebdocChatInterface) ctx;
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
        dbReference.child("Users").child(AppName).child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = (String) dataSnapshot.child("status").getValue();
                long lastSeen = (long) dataSnapshot.child("timestamp").getValue();
                listener.onChangeUserStatusResponse(status, String.valueOf(lastSeen));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onChangeUserStatusResponse(databaseError.getMessage().toString(), "null");
            }
        });
    }

    private static void seenStatus(String AppName, final String personalEmail, final String chatUserEmail, String UsersChatKey) {
        DatabaseReference changeMsgSeenStatusReference = FirebaseDatabase.getInstance().getReference();
        changeMsgSeenStatusReference.child("Messages").child(AppName).child(UsersChatKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageDataModel messages = snapshot.getValue(MessageDataModel.class);
                    if (messages.getReceiver().equals(personalEmail) && messages.getSender().equals(chatUserEmail)) {
                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put("MessageStatus", "seen");
                        snapshot.getRef().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    private static void sendNotification(final String senderAppName, final String receiverAppName, final String sender, final String receiver, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens").child(receiverAppName);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                APIService apiService;
                apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(sender, sender + ": " + msg, senderAppName, "Sent");
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            //Toast.makeText(MessagesActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable throwable) {
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void updateToken(String AppName, String Userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(FirebaseInstanceId.getInstance().getToken());
        reference.child(AppName).child(Userid).setValue(token1);
    }

    private static String messageSend(final String senderAppName, final String receiverAppName, final String msg, final String sender, final String receiver, String msgType) {
        final boolean[] notify = {false};
        notify[0] = true;
        String UsersChatKey = "";
        final String[] response = {""};
        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference();
        String messageID = chatReference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("message", msg);
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("timestamp", ServerValue.TIMESTAMP);
        hashMap.put("messageID", messageID);
        hashMap.put("type", msgType);
        hashMap.put("MessageStatus", "sent");
        HashMap<String, Object> chat_hashMap = new HashMap<String, Object>();
        chat_hashMap.put("chat", "true");
        chatReference.child("Chat").child(receiver).child(senderAppName).child(sender).updateChildren(chat_hashMap);
        ArrayList TwoChattingUsersID = new ArrayList<>();
        TwoChattingUsersID.add(sender);
        TwoChattingUsersID.add(receiver);
        Collections.sort(TwoChattingUsersID);
        UsersChatKey = TwoChattingUsersID.get(0) + "_" + TwoChattingUsersID.get(1);
        chatReference.child("Messages").child(senderAppName).child(UsersChatKey).child(messageID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    response[0] = "success";
                    if (notify[0]) {
                        sendNotification(senderAppName, receiverAppName, sender, receiver, msg);
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

        //final FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        final RegisterUserForChatInterface registerUserForChatInterface = (RegisterUserForChatInterface) context;
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //DatabaseReference firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(appName).child(email.replace(".", ""));
                            DatabaseReference firebaseDatabaseReference = reference.getReference("Users").child(appName).child(email.replace(".", ""));
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put("name", name);
                            hashMap.put("email", email);
                            hashMap.put("status", "online");

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
                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                registerUserForChatInterface.RegisterUserResponse("successfully login");
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

    public static void getChatUsersList(Context context, final String email, String appName) {
        final WebdocChatUsersInterface vetDocChatUsersInterface = (WebdocChatUsersInterface) context;
        if (!(appName.equalsIgnoreCase(String.valueOf(R.string.app_name)))) {
            DatabaseReference chatUsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(R.string.app_name));
            chatUsersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Global.ChatUsersList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatUserModel user = new ChatUserModel();

                        user.setFirebaseID(snapshot.getKey());
                        // Toast.makeText(UserDashboardActivity.this, snapshot.getKey(), Toast.LENGTH_SHORT).show();
                        user.setUserName(snapshot.child("username").getValue().toString());
                        user.setUserID(snapshot.child("userID").getValue().toString());
                        user.setFirebaseID(snapshot.child("firebaseID").getValue().toString());
                        user.setStatus(snapshot.child("status").getValue().toString());
                        user.setAppName(String.valueOf(R.string.app_name));
                        Global.ChatUsersList.add(user);
                    }
                    //DoctorsListFrag.doctorsListAdapter.notifyDataSetChanged();
                    vetDocChatUsersInterface.ChatUsers(Global.ChatUsersList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
            dbReference.child("Chat").child(email.replace(".", "")).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        dbReference.child("Chat").child(email.replace(".", "")).child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                                for (final DataSnapshot snapshot1 : dataSnapshot1.getChildren()) {
                                    dbReference.child("Users").child(snapshot.getKey()).child(snapshot1.getKey()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            ChatUserModel tempUser = new ChatUserModel();
                                            tempUser.setStatus(dataSnapshot.child("status").getValue().toString());
                                            tempUser.setUserName(dataSnapshot.child("username").getValue().toString());
                                            tempUser.setAppName(snapshot.getKey());
                                            tempUser.setFirebaseID(snapshot1.getKey());
                                            tempUser.setUserID(dataSnapshot.child("userID").getValue().toString());
                                            Global.ChatUsersList.add(tempUser);
                                       /*Toast.makeText(getActivity(), Global.chatUsersList.toString(), Toast.LENGTH_LONG).show();*/
                                           /* if (ChatUsersListFrag.adapter != null) {
                                                ChatUsersListFrag.adapter.notifyDataSetChanged();
                                            }*/
                                            vetDocChatUsersInterface.ChatUsers(Global.ChatUsersList);

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

    private static FirebaseApp firebaseAppReference(Context context)
    {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyAWhpWnFmjGfkBEfLe2PfuypOYyGcH84LA")
                .setApplicationId("1:788347610980:android:cfea43ffde6fb4e25cfc71")
                .setDatabaseUrl("https://webdocdoctorsdk.firebaseio.com")
                .build();

        FirebaseApp secondApp = FirebaseApp.initializeApp(context, options, "second app");

        return secondApp;
    }

}
