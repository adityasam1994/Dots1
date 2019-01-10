package com.example.aditya.dots1;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class findprovider extends Service {

    final static String MY_ACTION= "MY_ACTION";
    private Timer mytimer;
    String tvservice,co,uids;
    long StartTime;
    Boolean killtimer=false;
    double lati,longi;
    DatabaseReference dbr;
    DatabaseReference dbruser;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    Boolean provider_found=false;
    Boolean request_rejected=false;
    Boolean pending=true, order_cancelled=false;
    double provider_distance;
    List<String> rejected_providers=new ArrayList<String>();

    public findprovider() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        //Toast.makeText(this, "Searching for provider", Toast.LENGTH_SHORT).show();

        co=intent.getExtras().getString("code");
        lati=intent.getExtras().getDouble("lat");
        longi=intent.getExtras().getDouble("lng");
        tvservice=intent.getExtras().getString("tvservice");

        dbr=FirebaseDatabase.getInstance().getReference("Orders");
        dbruser=FirebaseDatabase.getInstance().getReference("Users");

        search_for_provider();

        order_accept();

        return START_STICKY;

    }

    private void order_accept() {
        dbr.child(fauth.getCurrentUser().getUid()).child(co).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dd : dataSnapshot.getChildren()) {

                            int na=dd.getKey().toString().length();

                            if(na > 20) {
                                if (dd.child("status").getValue().toString().equals("accepted")) {
                                    provider_found = true;
                                    Intent intent = new Intent(findprovider.this, order_accepted.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("pid", dd.getKey().toString());
                                    intent.putExtra("oid",co);
                                    startActivity(intent);
                                    stopSelf();

                                }
                                if(dd.child("status").getValue().toString().equals("cancelled")){
                                    order_cancelled=true;
                                    Toast.makeText(findprovider.this, "Order Cancelled!", Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(findprovider.this, newdrawer.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    stopSelf();
                                }
                                if(dd.child("status").getValue().toString().equals("rejected")){
                                    rejected_providers.add(dd.getKey().toString());
                                    request_rejected=true;
                                }
                                if(dd.child("status").getValue().toString().equals("pending")){
                                    pending=true;
                                }else{
                                    pending=false;
                                }

                            }

                }

                if(provider_found.equals(false) && pending.equals(false) && order_cancelled.equals(false)){
                    search_for_provider();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private  void search_for_provider(){
        final double[] dist = {50000};
        uids=null;
        dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String st = ds.child("status").getValue().toString();

                    if(provider_found.equals(false) && !rejected_providers.contains(ds.getKey().toString())
                            && !fauth.getCurrentUser().getUid().equals(ds.getKey().toString())) {

                        if (st.equals("provider")) {
                            String ser = ds.child("info").child("eservice").getValue().toString();
                            String rs = tvservice;

                            if (ser.equals(rs)) {

                                double lat = (double) ds.child("lati").getValue();
                                double lng = (double) ds.child("longi").getValue();

                                double clat = lati;
                                double clng = longi;

                                Location locc = new Location("");
                                locc.setLatitude(clat);
                                locc.setLongitude(clng);

                                Location locp = new Location("");
                                locp.setLatitude(lat);
                                locp.setLongitude(lng);

                                double distance = locc.distanceTo(locp);

                                if (distance < dist[0]) {
                                    dist[0] = distance;
                                    provider_distance=dist[0];
                                    uids = ds.getKey().toString();
                                }

                            }
                        }
                    }
                }
                String code = co;
                if(uids != null) {
                    dbr.child(fauth.getCurrentUser().getUid()).child(code).child(uids).child("status").setValue("pending");
                    StartTime= SystemClock.uptimeMillis();
                    request_rejected = false;
                    killtimer=false;
                    Timer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Mthread mthread=new Mthread();
        mthread.start();
    }

    public class Mthread extends Thread{
        @Override
        public void run() {
            try {

                int delay=1000;
                int period=2*1000;
                mytimer = new Timer();
                mytimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent=new Intent();
                        intent.setAction(MY_ACTION);

                        double diss=provider_distance/1000;
                        diss=diss*100;
                        diss=Math.round(diss);
                        diss=diss/100;
                        String d=""+diss +" Km";

                        if(uids != null){
                            intent.putExtra("dist", d);
                        }
                        else {
                            intent.putExtra("dist", "Sorry! Currently no provider is avilable");
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

    private void Timer(){
        final Handler handler=new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(findprovider.this, ""+(SystemClock.uptimeMillis()-StartTime), Toast.LENGTH_SHORT).show();

                if((SystemClock.uptimeMillis() - StartTime) > 60000 && killtimer.equals(false)){

                    DatabaseReference d = dbr.child(fauth.getCurrentUser().getUid()).child(co).child(uids);

                    Map<String, Object> updates=new HashMap<String, Object>();

                    updates.put("status","rejected");

                    d.updateChildren(updates);

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
