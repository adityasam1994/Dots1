package com.example.aditya.dots1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class order_status_service extends Service {

    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Orders");
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    String oid;
    String lastpage;
    public order_status_service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        lastpage=intent.getExtras().getString("lastpage");
        oid=intent.getExtras().getString("oid");
        dbr.child(fauth.getCurrentUser().getUid()).child(oid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dd : dataSnapshot.getChildren()) {

                            int na=dd.getKey().toString().length();

                            if(na > 20) {
                                String test=dd.child("status").getValue().toString();

                                if (test.equals("accepted")) {
                                    Intent intent = new Intent(order_status_service.this, order_accepted.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("pid", dd.getKey().toString());
                                    intent.putExtra("lastpage",lastpage);
                                    intent.putExtra("oid",oid);
                                    startActivity(intent);
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
}
