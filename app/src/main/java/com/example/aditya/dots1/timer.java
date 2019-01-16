package com.example.aditya.dots1;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class timer extends AppCompatActivity {

    SharedPreferences pref;
    Boolean timerrunning,startRun, killrun=false;
    Button btnstart;
    TextView timer;
    Handler handler;
    long StartTime,MillisecondTime,UpdateTime=0L,TimeBuff;
    int Seconds,Minutes,MilliSeconds,hours,Second;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    DatabaseReference dbr=FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference dbruser=FirebaseDatabase.getInstance().getReference("Users");
    String orderpath,time, status="";
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        sharedPreferences=getSharedPreferences("TimerData", Context.MODE_PRIVATE);
        /*SharedPreferences.Editor edi=sharedPreferences.edit();
        edi.putBoolean("timerrunning", true);
        edi.commit();*/

        orderpath=getIntent().getExtras().getString("orderpath");
        //Toast.makeText(this, ""+orderpath, Toast.LENGTH_SHORT).show();

        SimpleDateFormat forma=new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        Date cd= Calendar.getInstance().getTime();
        final String dt=forma.format(cd);

        dbruser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("order_in_progress").setValue(orderpath);

        dbr.child(orderpath).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("start_time").setValue(dt);

        btnstart=(Button)findViewById(R.id.btnpay);
        timer=(TextView) findViewById(R.id.tvTimer);
        pref = getSharedPreferences("pref", 0);
        Timer();

        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markcomplete();
            }
        });
    }

    private void markcomplete() {
        final Dialog dialog=new Dialog(timer.this);
        dialog.setContentView(R.layout.timer_end);
        dialog.show();

        TextView end=dialog.findViewById(R.id.tvend);
        TextView can=dialog.findViewById(R.id.tvcancel);

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pref.edit().remove("endtime").commit();
                //sharedPreferences.edit().putBoolean("timerrunning",false).commit();

                SimpleDateFormat forma=new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                Date cd= Calendar.getInstance().getTime();
                final String dt=forma.format(cd);

                dbr.child(orderpath).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("end_time").setValue(dt);

                killrun=true;
                dialog.dismiss();

                timesetter ts=new timesetter(time.toString());

                dbr.child(orderpath).child("stopwatch").setValue(ts)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent=new Intent(timer.this, job_done.class);
                                startActivity(intent);
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(timer.this, "Failed"+e, Toast.LENGTH_SHORT).show();
                    }
                });

                dbr.child(orderpath).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("completed");
                dbruser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("order_in_progress").removeValue();
            }
        });

        can.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void Timer(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(killrun.equals(false))
                {
                    StartTime = pref.getLong("endtime", SystemClock.uptimeMillis());
                    long et = SystemClock.uptimeMillis();
                    MillisecondTime = SystemClock.uptimeMillis() - StartTime;
                    UpdateTime = MillisecondTime / 1000;
                    Seconds = (int) UpdateTime;
                    hours = Seconds / 3600;
                    Minutes = (Seconds) / 60;
                    Second = Seconds % 60;

                    time = String.format("" + hours + ":" + Minutes + ":" + "%02d", Second);

                    timer.setText(time);
                    handler.postDelayed(this, 100);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putLong("endtime", StartTime);
                    editor.commit();

                    //dbr.child(orderpath).child("timer").setValue(time);
                }
                if(killrun.equals(true)){
                    handler.removeCallbacks(this);
                }

            }

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor edi=sharedPreferences.edit();
        edi.putString("orderpath", orderpath);
        edi.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent=new Intent(this, provider_home.class);
        startActivity(intent);
    }
}
