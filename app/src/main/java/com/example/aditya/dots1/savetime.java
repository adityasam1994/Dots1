package com.example.aditya.dots1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class savetime extends Service {

    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Orders");

    public savetime() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String orderpath=intent.getExtras().getString("orderpath");
        String time=intent.getExtras().getString("time");
        timesetter ts=new timesetter(time.toString());

        dbr.child(orderpath).child("stopwatch").setValue(ts)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(savetime.this, "Added time successfully", Toast.LENGTH_SHORT).show();
            }
        });

        return START_NOT_STICKY;
    }
}
