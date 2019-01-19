package com.example.aditya.dots1;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class customer_notification_service extends Service {

    DatabaseReference dbrorder= FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference dbruser=FirebaseDatabase.getInstance().getReference("Users");
    private Timer mytimer;
    final static String MY_ACTION= "MY_ACTION";
    String cod;
    String tvservice,co,uids, message, nordertime;
    double lati, longi;
    DatabaseReference dbr;
    List<String> rejected_providers=new ArrayList<>();
    Boolean pending=false, killtimer=false;
    double distance, plat, plng;
    SharedPreferences sprefappopen;

    public customer_notification_service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sprefappopen=getSharedPreferences("appopen", Context.MODE_PRIVATE);
        co=intent.getExtras().getString("code");
        lati=intent.getExtras().getDouble("lat");
        longi=intent.getExtras().getDouble("lng");
        tvservice=intent.getExtras().getString("tvservice");

        search_provider();

        dbrorder.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(co).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.getKey().toString().length() > 20){
                        String status=ds.child("status").getValue().toString();

                        if(status.equals("accepted")){
                            sprefappopen.edit().putBoolean("accepted", true).commit();
                            Intent intent=new Intent(getApplicationContext(), order_accepted.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("pid", ds.getKey().toString());
                            intent.putExtra("oid", co);
                            intent.putExtra("lastpage", "statuspage");
                            startActivity(intent);
                            break;
                        }

                        else if(status.equals("cancelled")){
                            sprefappopen.edit().putBoolean("cancelled", true).commit();
                            Toast.makeText(customer_notification_service.this, "Order Cancelled", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(getApplicationContext(), newdrawer.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            stopSelf();
                            break;
                        }

                        else if(status.equals("rejected")){
                            rejected_providers.add(ds.getKey().toString());

                            Set<String> taskset = new HashSet<String>(rejected_providers);
                            sprefappopen.edit().putStringSet("rej", taskset).commit();

                            pending=false;
                        }

                        else if(status.equals("pending")){
                            pending=true;
                            break;
                        }
                    }
                }
                if(!pending){
                    search_provider();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return START_STICKY;
    }


    private void search_provider() {

        distance=50000;

        dbruser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uids=null;
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.child("status").getValue().toString().equals("provider")){
                        if(ds.child("info").child("eservice").getValue().toString().equals(tvservice)) {

                            Set<String> taskset = sprefappopen.getStringSet("rej", new HashSet<String>());
                            List<String> tasklist = new ArrayList<>(taskset);

                            if (!tasklist.contains(ds.getKey().toString())) {

                                plat = (double) ds.child("info").child("lati").getValue();
                                plng = (double) ds.child("info").child("longi").getValue();

                                double clat = lati;
                                double clng = longi;

                                Location locc = new Location("");
                                locc.setLatitude(clat);
                                locc.setLongitude(clng);

                                Location locp = new Location("");
                                locp.setLatitude(plat);
                                locp.setLongitude(plng);

                                double dist = locc.distanceTo(locp);

                                if (dist < distance) {
                                    distance = dist;
                                    uids = ds.getKey().toString();
                                }
                            }
                        }
                    }
                }
                SimpleDateFormat forma=new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                Date cd= Calendar.getInstance().getTime();
                final String dt=forma.format(cd);

                if(uids != null){
                    dbrorder.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(co).child(uids).child("status").setValue("pending");
                    dbrorder.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(co).child(uids).child("time").setValue(dt);
                    sprefappopen.edit().putLong("StartTime", SystemClock.uptimeMillis()).commit();
                    sprefappopen.edit().putString("uids", uids).commit();

                    sprefappopen.edit().putLong("lat", Double.doubleToRawLongBits(plat)).commit();
                    sprefappopen.edit().putLong("lng", Double.doubleToRawLongBits(plng)).commit();

                    killtimer=false;
                    Timer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        MyThread myThread=new MyThread();
        myThread.start();

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

                        double diss = distance/ 1000;
                        diss = diss * 100;
                        diss = Math.round(diss);
                        diss = diss / 100;
                        String d = "" + diss + " Km";

                        if(uids != null) {
                            intent.putExtra("dist", d);
                            intent.putExtra("lat", plat);
                            intent.putExtra("lng", plng);
                        }
                        else {
                            intent.putExtra("dist","Not found");
                            intent.putExtra("lat", 0);
                            intent.putExtra("lng", 0);
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

    private  void Timer(){
        final Handler handler=new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                String ui=sprefappopen.getString("uids", null);

                if((SystemClock.uptimeMillis() - sprefappopen.getLong("StartTime", SystemClock.uptimeMillis())) > 20000 && !killtimer
                        && !sprefappopen.getBoolean("accepted", false) && !sprefappopen.getBoolean("cancelled", false)){
                    String u=sprefappopen.getString("uids",null);
                    dbrorder.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(co).child(u).child("status").setValue("rejected");
                    killtimer=true;
                }

                handler.postDelayed(this, 100);

                if(killtimer.equals(true)){
                    handler.removeCallbacks(this);
                }

            }
        });
    }
}
