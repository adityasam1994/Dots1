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

import java.util.Timer;
import java.util.TimerTask;

import static com.example.aditya.dots1.App.CHANNEL_ID;

public class testsevice extends Service {

    private Timer mytimer;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference dbrorder= FirebaseDatabase.getInstance().getReference("Orders");
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    SharedPreferences sharedPreferences;
    final static String MY_ACTIONS="MY_ACTIONS";
    double StartTime;
    Boolean fromreceiver=true;
    SharedPreferences spref;
    int ordercount;
    String message;
    double lat=0,lng=0;
    String fname="",emailid="",cod="",detail="",
            tim="",commen="",cname="",caddress="",cservice="",uids="",format="",username="",
            order_path="",secretcode="",orderstatus="",customerid="", servicetype="", ordertime="";

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
        spref=getSharedPreferences("notification", Context.MODE_PRIVATE);

        message="Looking for new orders";

        final Intent intent1 = new Intent(testsevice.this, provider_home.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(testsevice.this, 2, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification builder = new NotificationCompat.Builder(testsevice.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.iconfinder_notification)
                .setContentTitle("New Order")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0L})
                .build();

        startForeground(1, builder);

        StartTime= SystemClock.uptimeMillis();

        dbrorder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordercount=0;
                useridloop:
                for(DataSnapshot dsusers : dataSnapshot.getChildren()){
                    for(DataSnapshot dsorders : dsusers.getChildren()){
                        for (DataSnapshot dselements : dsorders.getChildren()){
                            if(dselements.getKey().toString().equals(fauth.getCurrentUser().getUid())){
                                if(dselements.child("status").getValue().toString().equals("pending")) {
                                    fname = dsorders.child("username").getValue().toString();

                                    ordercount = ordercount+1;

                                    orderstatus=dselements.child("status").getValue().toString();
                                    lat= (double) dsorders.child("latitude").getValue();
                                    lng= (double) dsorders.child("longitude").getValue();
                                    username = dsorders.child("username").getValue().toString();
                                    cod = dsorders.child("code").getValue().toString();
                                    commen = dsorders.child("ecomment").getValue().toString();
                                    caddress = dsorders.child("eaddress").getValue().toString();
                                    cservice = dsorders.child("service").getValue().toString();
                                    tim = dsorders.child("time").getValue().toString();
                                    format = dsorders.child("format").getValue().toString();
                                    servicetype=dsorders.child("servicetype").getValue().toString();
                                    secretcode = dsorders.child("qrcode").getValue().toString();
                                    customerid=dsusers.getKey().toString();
                                    order_path = dsusers.getKey().toString() + "/" + dsorders.getKey().toString();
                                    ordertime=dsorders.child("Date").getValue().toString();

                                    break useridloop;

                                }
                                else {
                                    cod="";
                                }
                            }
                        }
                    }
                }
                if(ordercount == 1){
                    message="You have an order from "+fname;
                }
                if(ordercount == 0){
                    message="You don't have any new order";
                }
                else {
                    message="You have "+ordercount+" new order";
                }
                //Boolean appopen = sharedPreferences.getBoolean("provider_at_home", false);

                if(!spref.getString("text","").equals(message)) {
                    final Intent intent1 = new Intent(testsevice.this, provider_home.class);
                    final PendingIntent pendingIntent = PendingIntent.getActivity(testsevice.this, 2, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification builder = new NotificationCompat.Builder(testsevice.this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.iconfinder_notification)
                            .setContentTitle("New Order")
                            .setContentText(message)
                            .setContentIntent(pendingIntent)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .build();

                    startForeground(1, builder);
                    spref.edit().putString("text", message).commit();
                }
                else {
                    final Intent intent1 = new Intent(testsevice.this, provider_home.class);
                    final PendingIntent pendingIntent = PendingIntent.getActivity(testsevice.this, 2, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification builder = new NotificationCompat.Builder(testsevice.this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.iconfinder_notification)
                            .setContentTitle("New Order")
                            .setContentText(message)
                            .setContentIntent(pendingIntent)
                            .setVibrate(new long[]{0L})
                            .build();

                    startForeground(1, builder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Intent intent=new Intent(getApplicationContext(),MyReceiver.class);
                sendBroadcast(intent);
            }
        });

        TestThread testThread=new TestThread();
        testThread.start();

        return START_STICKY;
    }

    public class TestThread extends Thread{
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
                        intent.setAction(MY_ACTIONS);
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
                            intent.putExtra("ordertime", ordertime);
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
