package com.example.aditya.dots1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.aditya.dots1.App.CHANNEL_ID;

public class testsevice extends Service {

    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference dbrorder= FirebaseDatabase.getInstance().getReference("Orders");
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    SharedPreferences sharedPreferences;
    double StartTime;
    Boolean fromreceiver=true;
    int ordercount;

    public testsevice() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        sharedPreferences=getSharedPreferences("appopen", Context.MODE_PRIVATE);

        StartTime= SystemClock.uptimeMillis();
        //Timer();

        dbrorder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordercount=0;
                for(DataSnapshot dsusers : dataSnapshot.getChildren()){
                    for(DataSnapshot dsorders : dsusers.getChildren()){
                        for (DataSnapshot dselements : dsorders.getChildren()){
                            if(dselements.getKey().toString().equals(fauth.getCurrentUser().getUid())){
                                if(dselements.child("status").getValue().toString().equals("pending")) {
                                    String fname = dsorders.child("username").getValue().toString();

                                    ordercount = ordercount+1;
                                    /*final Intent intent1 = new Intent(testsevice.this, provider_home.class);
                                    final PendingIntent pendingIntent = PendingIntent.getActivity(testsevice.this, 2, intent1, PendingIntent.FLAG_UPDATE_CURRENT);*/

                                    Boolean appopen = sharedPreferences.getBoolean("provider_at_home", false);

                                    //if (!appopen) {

                                        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(testsevice.this, CHANNEL_ID)
                                                .setSmallIcon(R.drawable.iconfinder_notification)
                                                .setContentTitle("New Order")
                                                .setContentText("You have a new order from " + fname)
                                                .setAutoCancel(true)
                                                .setContentIntent(pendingIntent);

                                        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.cancelAll();
                                        notificationManager.notify(startId, builder.build());*/
                                    //}

                                }
                            }
                        }
                    }
                }
                final Intent intent1 = new Intent(testsevice.this, provider_home.class);
                final PendingIntent pendingIntent = PendingIntent.getActivity(testsevice.this, 2, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification builder = new NotificationCompat.Builder(testsevice.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.iconfinder_notification)
                        .setContentTitle("New Order")
                        .setContentText("You have "+ordercount+" new order")
                        .setContentIntent(pendingIntent)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .build();

                startForeground(1, builder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return START_STICKY;
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(this,MyReceiver.class);
        sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent intent=new Intent(this,MyReceiver.class);
        sendBroadcast(intent);
    }*/

    private void Timer(){
        final Handler handler=new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if((SystemClock.uptimeMillis() - StartTime) > 120000){
                    //Toast.makeText(testsevice.this, ""+(SystemClock.uptimeMillis() - StartTime), Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(testsevice.this,MyReceiver.class);
                    sendBroadcast(intent);
                }

                handler.postDelayed(this, 100);
            }
        });
    }
}
