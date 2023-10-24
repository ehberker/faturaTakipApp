package com.fatura.faturatakipapp;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    List<String> sentDocId = new ArrayList<>();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {



        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
               sentDocId.clear();
            }

        }, 420000, 90000);



        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            handleNow();


        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());



            FirebaseFirestore db = FirebaseFirestore.getInstance();

            CollectionReference dbCol = db.collection("faturalar");

            dbCol.orderBy("tarih", Query.Direction.ASCENDING).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    if (String.valueOf(document.get("durum")).equals("ÖDENMEDİ")){
                                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");

                                        String dateStop = String.valueOf(document.get("tarih"));
                                        int x  = (int)(Math.random() * 500000 + 1);

                                        //HH converts hour in 24 hours format (0-23), day calculation
                                        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

                                        Date d1 = new Date();
                                        Date d2 = null;

                                        try {
                                            d2 = formatter.parse(dateStop);

                                            long diff = d2.getTime() - d1.getTime();
                                            long diffDays = diff / (24 * 60 * 60 * 1000);

                                            if (diffDays < 3 && !sentDocId.contains(document.getId())){
                                                sendNotification( String.valueOf(document.get("tarih")) + " son ödeme tarihli "  + String.valueOf(document.get("tutar"))+ " TL tutarında ödenmemiş bir "+  String.valueOf(document.get("tur"))+ " faturanız bulunmaktadır." +
                                                        "   ",String.valueOf(x),document.getId());
                                                sentDocId.add(document.getId());
                                                return;
                                            }

                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                }
                            }}});

        }
 }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        sendRegistrationToServer(token);
    }
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendRegistrationToServer(String token) {
    }

    private void sendNotification(String messageBody,String channelId,String docId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Intent odeIntent = new Intent(this, OdeActivity.class);
        odeIntent.putExtra("docId",docId);
        PendingIntent pendingIntentOde = PendingIntent.getActivity(this, 0 /* Request code */, odeIntent,
                PendingIntent.FLAG_ONE_SHOT);



        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setContentTitle("Hatırlatma")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .addAction(R.drawable.ic_notif,"ÖDENDİ",pendingIntentOde)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        int id = (int)(Math.random() * 500000 + 1);

        notificationManager.notify( id/* ID of notification */, notificationBuilder.build());
    }
}