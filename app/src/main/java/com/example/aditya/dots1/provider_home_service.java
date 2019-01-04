package com.example.aditya.dots1;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class provider_home_service extends Service {

    private Timer mytimer;
    final static String MY_ACTION= "MY_ACTION";
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Orders");
    String fname="",emailid="",cod="",detail="",
            tim="",commen="",cname="",caddress="",cservice="",uids="",format="",username="",
            order_path="",secretcode="",orderstatus="",customerid="", servicetype="";
    double lat=0,lng=0;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    public provider_home_service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                useridloop:
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    for (DataSnapshot d : ds.getChildren()){
                        for (DataSnapshot dd : d.getChildren()){
                            if(dd.getKey().equals(fauth.getCurrentUser().getUid())){
                                if(dd.child("status").getValue().toString().equals("pending"))
                                {
                                    orderstatus=dd.child("status").getValue().toString();
                                    lat= (double) d.child("latitude").getValue();
                                    lng= (double) d.child("longitude").getValue();
                                    username = d.child("username").getValue().toString();
                                    cod = d.child("code").getValue().toString();
                                    commen = d.child("ecomment").getValue().toString();
                                    caddress = d.child("eaddress").getValue().toString();
                                    cservice = d.child("service").getValue().toString();
                                    tim = d.child("time").getValue().toString();
                                    format = d.child("format").getValue().toString();
                                    servicetype=d.child("servicetype").getValue().toString();
                                    secretcode = d.child("qrcode").getValue().toString();
                                    customerid=ds.getKey().toString();
                                    order_path = ds.getKey().toString() + "/" + d.getKey().toString();

                                    break useridloop;

                                }
                                else {
                                    cod="";
                                }
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        MyThread myThread=new MyThread();
        myThread.start();

        return START_STICKY;
    }

    public  class MyThread extends Thread{
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
                        if(cod != null) {
                            intent.putExtra("cod", cod);
                            intent.putExtra("orderstatus", orderstatus);
                            intent.putExtra("lat", lat);
                            intent.putExtra("lng", lng);
                            intent.putExtra("username", username);
                            intent.putExtra("commen", commen);
                            intent.putExtra("caddress", caddress);
                            intent.putExtra("cservice", cservice);
                            intent.putExtra("tim", tim);
                            intent.putExtra("format", format);
                            intent.putExtra("secretcode", secretcode);
                            intent.putExtra("customerid", customerid);
                            intent.putExtra("order_path", order_path);
                            intent.putExtra("servicetype",servicetype);
                        }
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
