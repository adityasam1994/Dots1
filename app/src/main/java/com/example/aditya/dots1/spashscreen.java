package com.example.aditya.dots1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class spashscreen extends Activity {

    private static int SPLASH_TIME_OUT=10000;
    String status;
    Boolean ready=false;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        if(fauth.getCurrentUser() != null) {
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

        }else {
            startActivity(new Intent(spashscreen.this, newlogin.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }



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
}
