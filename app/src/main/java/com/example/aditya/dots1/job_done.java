package com.example.aditya.dots1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class job_done extends AppCompatActivity {

    private final int SPLASH_LENGTH=2000;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Orders");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.job_done);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(job_done.this,provider_home.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_LENGTH);
    }
}
