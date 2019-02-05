package com.example.aditya.dots1;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class spashscreen extends Activity {

    private static int SPLASH_TIME_OUT=2000;
    String status;
    Boolean ready=false;
    LinearLayout noconnection, logolayout;
    Button btnretry;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        btnretry=(Button)findViewById(R.id.btnretry);
        noconnection=(LinearLayout)findViewById(R.id.noconnection);
        logolayout=(LinearLayout)findViewById(R.id.logolayout);

        if(isNetworkAvailable()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0);
            noconnection.setLayoutParams(params);

            if (fauth.getCurrentUser() != null) {
                dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("status")) {
                            Intent intent = new Intent(spashscreen.this, newlogin.class);
                            startActivity(intent);
                        } else {
                            status = dataSnapshot.child(fauth.getCurrentUser().getUid()).child("status").getValue().toString();
                            if (status.equals("customer")) {
                                if (dataSnapshot.child(fauth.getCurrentUser().getUid()).hasChild("current_status")) {
                                    if (dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("customer")) {
                                        startActivity(new Intent(spashscreen.this, newdrawer.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    }
                                    if (dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("provider")) {
                                        startActivity(new Intent(spashscreen.this, provider_home.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    }
                                } else {
                                    startActivity(new Intent(spashscreen.this, newdrawer.class));
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            }
                            if (status.equals("provider")) {
                                if (dataSnapshot.child(fauth.getCurrentUser().getUid()).hasChild("current_status")) {
                                    if (dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("customer")) {
                                        startActivity(new Intent(spashscreen.this, newdrawer.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    }
                                    if (dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("provider")) {
                                        startActivity(new Intent(spashscreen.this, provider_home.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    }
                                } else {
                                    startActivity(new Intent(spashscreen.this, provider_home.class));
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else {

                 new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(spashscreen.this, newlogin.class));
                finish();
            }
        },SPLASH_TIME_OUT);
            }
        }
        else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0);
            logolayout.setLayoutParams(params);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.blink);
            ImageView imageView = (ImageView)findViewById(R.id.exclaimation);
            imageView.startAnimation(animation);
        }

        btnretry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });

       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(fauth.getCurrentUser() != null) {
                    if(status == "customer") {
                        startActivity(new Intent(spashscreen.this, newdrawer.class));
                    }
                    if(status == "provider"){
                        startActivity(new Intent(spashscreen.this,provider_home.class));
                    }
                }
                else {
                    startActivity(new Intent(spashscreen.this, newlogin.class));
                }
                finish();
            }
        },SPLASH_TIME_OUT);*/


    }
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return  activeNetworkInfo != null;
    }
}
