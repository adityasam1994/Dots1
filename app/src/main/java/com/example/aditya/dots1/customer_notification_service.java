package com.example.aditya.dots1;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Timer;
import java.util.TimerTask;

public class customer_notification_service extends Service {

    DatabaseReference dbrorder= FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference dbruser=FirebaseDatabase.getInstance().getReference("Users");
    private Timer mytimer;
    final static String MY_ACTION= "MY_ACTION";
    String cod;
    public customer_notification_service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();

        dbrorder.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    for(DataSnapshot dd:ds.getChildren()){
                        int le=dd.getKey().toString().length();
                        if(le > 15){
                            if(dd.child("status").getValue().toString().equals("completed")) {
                                cod=ds.getKey().toString();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return START_STICKY;
    }

    public class MyThread extends Thread{
        @Override
        public void run() {
            try{

                int delay=1000;
                int period=2*1000;
                mytimer = new Timer();
                mytimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent=new Intent();
                        intent.setAction(MY_ACTION);
                        intent.putExtra("orderid", cod);
                        sendBroadcast(intent);
                    }
                },delay,period);

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
