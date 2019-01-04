package com.example.aditya.dots1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class testsevice extends Service {

    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");


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
    public int onStartCommand(Intent intent, int flags, final int startId) {

        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fname=dataSnapshot.child("4bEiELg5XHUQUIHicIvg6Gv8AQ23").child("fname").getValue().toString();
                //Toast.makeText(testsevice.this, fname, Toast.LENGTH_SHORT).show();

                final Intent intent1=new Intent(testsevice.this,provider_home.class);
                final PendingIntent pendingIntent=PendingIntent.getActivity(testsevice.this,2,intent1,PendingIntent.FLAG_CANCEL_CURRENT);

                NotificationCompat.Builder builder=new NotificationCompat.Builder(testsevice.this)
                        .setSmallIcon(R.drawable.cast_ic_notification_small_icon).setContentTitle("New order")
                        .setContentText("You have a new order from "+fname)
                        .setContentIntent(pendingIntent);

                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(startId,builder.build());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return START_STICKY;
    }

    @Override
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
    }
}
