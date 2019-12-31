package com.webdocchat;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
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
import com.webdocchat.Models.CallHistoryDataModel;
import com.webdocchat.Models.ChatUserModel;
import com.webdocchat.Models.MessageDataModel;
import com.webdocchat.NotificationManager.APIService;
import com.webdocchat.NotificationManager.Client;
import com.webdocchat.NotificationManager.Data;
import com.webdocchat.NotificationManager.MyResponse;
import com.webdocchat.NotificationManager.Sender;
import com.webdocchat.NotificationManager.Token;
import com.webdocchat.TimeAgo.TimeAgo;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

            DatabaseReference ref = databaseReference.getReference().child("Messages");
            String messageID = ref.push().getKey();

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
                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

        ArrayList TwoChattingUsersID = new ArrayList<>();
        TwoChattingUsersID.add(personalEmail);
        TwoChattingUsersID.add(chatUserEmail);
        Collections.sort(TwoChattingUsersID);

        String chatKey = TwoChattingUsersID.get(0) + "_" + TwoChattingUsersID.get(1);

        chatReference.child("Messages").child(AppName).child(chatKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                msgData.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageDataModel msg = new MessageDataModel();
                    msg.setSender(snapshot.child("sender").getValue().toString());
                    msg.setReceiver(snapshot.child("receiver").getValue().toString());
                    msg.setMessage(snapshot.child("message").getValue().toString());
                    msg.setTimestamp(Long.parseLong(snapshot.child("timestamp").getValue().toString()));
                    msg.setType(snapshot.child("type").getValue().toString());
                    msg.setMessageStatus(snapshot.child("MessageStatus").getValue().toString());
                    msg.setMessageID(snapshot.child("messageID").getValue().toString());
                    msgData.add(msg);
                }
                listener.getMessagesResponse(msgData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void userIsTyping(Context context, final String senderAppName, final String senderEmail, final String receiverEmail)
    {
        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        reference.getReference().child("Users").child(senderAppName).child(senderEmail).child("LastMessage").child(receiverEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("message"))
                {
                    final HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("type", "typing");
                    hashMap.put("message", "typing...");

                    String lastMessage = dataSnapshot.child("message").getValue().toString();
                    String lastMessageType = dataSnapshot.child("type").getValue().toString();

                    if(!lastMessageType.equalsIgnoreCase("typing"))
                    {
                        Global.lastMessage = lastMessage;
                        Global.lastMessageType = lastMessageType;
                    }

                    reference.getReference().child("Users").child(senderAppName).child(senderEmail).child("LastMessage").child(receiverEmail).updateChildren(hashMap);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //reference.getReference().child("Users").child(receiverAppName).child(receiverEmail).child("LastMessage").child(senderEmail).updateChildren(hashMap);
    }

    public static void userStoppedTyping(Context context, final String senderAppName, final String senderEmail, final String receiverEmail)
    {
        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        reference.getReference().child("Users").child(senderAppName).child(senderEmail).child("LastMessage").child(receiverEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("message"))
                {
                    String lastMessageType = dataSnapshot.child("type").getValue().toString();

                    if(lastMessageType.equalsIgnoreCase("typing"))
                    {
                        if(Global.lastMessageType != null && Global.lastMessage != null)
                        {
                            final HashMap<String, Object> hashMap = new HashMap<String, Object>();
                            hashMap.put("type", Global.lastMessageType);
                            hashMap.put("message", Global.lastMessage);

                            reference.getReference().child("Users").child(senderAppName).child(senderEmail).child("LastMessage").child(receiverEmail).updateChildren(hashMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static void lastMessage(FirebaseDatabase firebaseDatabase, String senderAppName, String receiverAppName, String senderEmail, String receiverEmail, String messageType, String message)
    {
        DatabaseReference reference = firebaseDatabase.getReference("Users");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("message", message);
        hashMap.put("type", messageType);
        hashMap.put("timestamp", ServerValue.TIMESTAMP);
        hashMap.put("sender", senderEmail);
        hashMap.put("receiver", receiverEmail);
        hashMap.put("MessageStatus", "sent");

        reference.child(senderAppName).child(senderEmail).child("LastMessage").child(receiverEmail).setValue(hashMap);

        reference.child(receiverAppName).child(receiverEmail).child("LastMessage").child(senderEmail).setValue(hashMap);
    }

    public static void lastMessageSeenStatus(Context context, final String senderAppName, final String receiverAppName, final String senderEmail, final String receiverEmail)
    {
        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        Global.lastMessageSeenReference = reference.getReference().child("Users").child(senderAppName).child(senderEmail).child("LastMessage").child(receiverEmail);
        Global.lastMessageSeenListener = Global.lastMessageSeenReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("receiver"))
                {
                    String receiver = dataSnapshot.child("receiver").getValue().toString();

                    if (receiver.equalsIgnoreCase(senderEmail))
                    {
                        final HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put("MessageStatus", "seen");

                        /* For last message sent and seen status */
                        reference.getReference().child("Users").child(senderAppName).child(senderEmail).child("LastMessage").child(receiverEmail).updateChildren(hashMap);
                        reference.getReference().child("Users").child(receiverAppName).child(receiverEmail).child("LastMessage").child(senderEmail).updateChildren(hashMap);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void stoplastMessageSeenStatus()
    {
        Global.lastMessageSeenReference.removeEventListener(Global.lastMessageSeenListener);
    }


    /* counter for unread messages */
    private static void unreadMessagesCounter(FirebaseDatabase firebaseDatabase, String appName, String senderEmail, String receiverEmail)
    {
        final DatabaseReference reference = firebaseDatabase.getReference("Users").child(appName)
                .child(senderEmail).child("UnreadMessages").child(receiverEmail);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("counter"))
                {
                    String unreadMessages = (String) dataSnapshot.child("counter").getValue();

                    int noOfmsgs = Integer.parseInt(unreadMessages);
                    noOfmsgs++;

                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("counter", String.valueOf(noOfmsgs));

                    reference.setValue(hashMap);
                }
                else
                {
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("counter", "1");

                    reference.setValue(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static void readMessagesCounter(final FirebaseDatabase firebaseDatabase, final String receiverAppName, final String senderEmail, final String receiverEmail)
    {
        final DatabaseReference reference = firebaseDatabase.getReference("Users").child(receiverAppName)
                .child(receiverEmail).child("UnreadMessages").child(senderEmail);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild("counter"))
                {
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("counter", "0");

                    firebaseDatabase.getReference("Users").child(receiverAppName)
                            .child(receiverEmail).child("UnreadMessages").child(senderEmail).updateChildren(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void changeStatus(Context ctx, String AppName, String UserId, String status)
    {
        FirebaseApp appReference = firebaseAppReference(ctx);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        DatabaseReference dbReference = reference.getReference().child("Users").child(AppName).child(UserId.replace(".",""));

        HashMap<String, Object> param = new HashMap<>();
        param.put("status", status);
        param.put("timestamp", ServerValue.TIMESTAMP);
        dbReference.updateChildren(param);
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
                listener.onUserStatusCheckResponse(status, TimeAgo.getTimeAgo(lastSeen, ctx));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onUserStatusCheckResponse(databaseError.getMessage().toString(), "null");
            }
        });
    }

    public static void stopSeenStatus()
    {
        Global.seenReference.removeEventListener(Global.seenListener);
    }

    public static void seenStatus(Context context, String AppName, final String personalEmail, final String chatUserEmail, final String senderAppName, final String receiverAppName) {

        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        ArrayList TwoChattingUsersID = new ArrayList<>();
        TwoChattingUsersID.add(personalEmail);
        TwoChattingUsersID.add(chatUserEmail);
        Collections.sort(TwoChattingUsersID);
        String UsersChatKey = TwoChattingUsersID.get(0) + "_" + TwoChattingUsersID.get(1);

        Global.seenReference = reference.getReference().child("Messages").child(AppName).child(UsersChatKey);
        Global.seenListener = Global.seenReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    MessageDataModel messages = snapshot.getValue(MessageDataModel.class);

                    if (messages.getReceiver().equals(personalEmail) && messages.getSender().equals(chatUserEmail))
                    {
                        final HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put("MessageStatus", "seen");
                        snapshot.getRef().updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful())
                                {
                                    readMessagesCounter(reference, receiverAppName, personalEmail, chatUserEmail);
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

    private static void messageSend(final Context context, final FirebaseDatabase reference, final String senderAppName, final String receiverAppName, final String msg, final String sender, final String receiver, final String msgType)
    {
        final boolean[] notify = {false};
        notify[0] = true;
        String UsersChatKey = "";

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
                if (task.isSuccessful())
                {
                    unreadMessagesCounter(reference, senderAppName, sender, receiver);

                    lastMessage(reference, senderAppName, receiverAppName, sender, receiver, msgType, msg);

                    if (notify[0])
                    {
                        sendNotification(context, reference, senderAppName, receiverAppName, sender, receiver, msg, msgType);
                    }
                    notify[0] = false;
                } else {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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

                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
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

            dbReference.child("Chat").child(email.replace(".", "")).child("PTCLHealth").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Global.chatIDs.clear();

                    for (final DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        Global.chatIDs.add(snapshot.getKey());

                        dbReference.child("Users").child("PTCLHealth").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Global.ChatUsersList.clear();

                                String unreadMessageCounter, lastMessageTimestamp, lastMessageType, lastMessage, lastMessageSender, lastMessageReceiver, lastMessageStatus, lastMessageTime = null;

                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {
                                    ChatUserModel user = new ChatUserModel();

                                    user.setStatus(snapshot.child("status").getValue().toString());
                                    user.setName(snapshot.child("name").getValue().toString());
                                    user.setAppName(snapshot.getKey());
                                    user.setFirebaseEmail(snapshot.getKey());
                                    user.setEmail(snapshot.child("email").getValue().toString());

                                    if(snapshot.child("UnreadMessages").hasChild(email))
                                    {
                                        unreadMessageCounter = (String) snapshot.child("UnreadMessages").child(email).child("counter").getValue();
                                    }
                                    else
                                    {
                                        /* default if UnreadMessages is not present in db */
                                        unreadMessageCounter = "0";
                                    }

                                    if(snapshot.child("LastMessage").hasChild(email))
                                    {
                                        lastMessageType = snapshot.child("LastMessage").child(email).child("type").getValue().toString();
                                        lastMessage = snapshot.child("LastMessage").child(email).child("message").getValue().toString();
                                        lastMessageTimestamp = snapshot.child("LastMessage").child(email).child("timestamp").getValue().toString();
                                        lastMessageSender = snapshot.child("LastMessage").child(email).child("sender").getValue().toString();
                                        lastMessageReceiver = snapshot.child("LastMessage").child(email).child("receiver").getValue().toString();
                                        lastMessageStatus = snapshot.child("LastMessage").child(email).child("MessageStatus").getValue().toString();
                                        lastMessageTime = getTime(Long.parseLong(lastMessageTimestamp));
                                    }
                                    else
                                    {
                                        lastMessageType = "no_type";
                                        lastMessage = "no_message";
                                        lastMessageTimestamp = "1576839469870";
                                        lastMessageSender = "no_sender";
                                        lastMessageReceiver = "no_receiver";
                                        lastMessageStatus = "no_status";
                                        lastMessageTime = "no_time";
                                    }

                                    user.setUnreadMessageCounter(unreadMessageCounter);
                                    user.setLastMessage(lastMessage);
                                    user.setLastMessageType(lastMessageType);
                                    user.setLastMessageTimestamp(lastMessageTimestamp);
                                    user.setLastMessageSender(lastMessageSender);
                                    user.setLastMessageReceiver(lastMessageReceiver);
                                    user.setLastMessageStatus(lastMessageStatus);
                                    user.setLastMessageTime(lastMessageTime);
                                    

                                    for (int i = 0; i < Global.chatIDs.size(); i++)
                                    {
                                        if (Global.chatIDs.get(i).equals(snapshot.getKey()))
                                        {
                                            Global.ChatUsersList.add(user);

                                            Collections.sort(Global.ChatUsersList, new Comparator<ChatUserModel>() {
                                                @Override
                                                public int compare(ChatUserModel user1, ChatUserModel user2) {
                                                    if (user1.getLastMessageTimestamp() == null || user2.getLastMessageTimestamp() == null)
                                                        return 0;
                                                    return user2.getLastMessageTimestamp().compareTo(user1.getLastMessageTimestamp());
                                                }
                                            });

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

    public static void callHistory(Context context, String email)
    {
        FirebaseApp appReference = firebaseAppReference(context);
        final FirebaseDatabase reference = FirebaseDatabase.getInstance(appReference);

        final WebdocCallHistoryInterface callHistoryInterface = (WebdocCallHistoryInterface) context;

        reference.getReference().child("WebDocCallHistory").child(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Global.callHistoryData.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    CallHistoryDataModel newItem = new CallHistoryDataModel();

                    newItem.setCallerId(snapshot.child("caller_id").getValue().toString());
                    newItem.setCallHistoryId(snapshot.child("id").getValue().toString());
                    newItem.setCallType(snapshot.child("callType").getValue().toString());
                    newItem.setDoctorId(snapshot.child("doctorId").getValue().toString());
                    newItem.setEstablishedTime(snapshot.child("EstablishedTime").getValue().toString());
                    newItem.setIsMissedCall(snapshot.child("isMissedCall").getValue().toString());
                    newItem.setTimeStamp(Long.parseLong(snapshot.child("timeStamp").getValue().toString()));

                    Global.callHistoryData.add(newItem);

                    callHistoryInterface.getCallHistoryResponse(Global.callHistoryData);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void isUserLoggedIn(Context context)
    {
        FirebaseApp appReference = firebaseAppReference(context);
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

    private static String getTime(long neededTimeMilis)
    {
        Calendar nowTime = Calendar.getInstance();
        Calendar neededTime = Calendar.getInstance();
        neededTime.setTimeInMillis(neededTimeMilis);

        if ((neededTime.get(Calendar.YEAR) == nowTime.get(Calendar.YEAR))) {

            if ((neededTime.get(Calendar.MONTH) == nowTime.get(Calendar.MONTH))) {

                if (neededTime.get(Calendar.DATE) - nowTime.get(Calendar.DATE) == 1) {
                    //here return like "Tomorrow at 12:00"
                    return "Tomorrow at " + DateFormat.format("hh:mm a", neededTime);

                } else if (nowTime.get(Calendar.DATE) == neededTime.get(Calendar.DATE)) {
                    //here return like "Today at 12:00"
                    return String.valueOf(DateFormat.format("hh:mm a", neededTime));

                } else if (nowTime.get(Calendar.DATE) - neededTime.get(Calendar.DATE) == 1) {
                    //here return like "Yesterday at 12:00"
                    return "yesterday";

                } else {
                    //here return like "May 31, 12:00"
                    return DateFormat.format("dd/MM/yyyy", neededTime).toString();
                }

            } else {
                //here return like "May 31, 12:00"
                return DateFormat.format("dd/MM/yyyy", neededTime).toString();
            }

        } else {
            //here return like "May 31 2010, 12:00" - it's a different year we need to show it
            return DateFormat.format("dd/MM/yyyy", neededTime).toString();
        }
    }

}
