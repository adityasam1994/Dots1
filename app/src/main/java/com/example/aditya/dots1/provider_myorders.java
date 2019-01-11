package com.example.aditya.dots1;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class provider_myorders extends AppCompatActivity {

    LinearLayout parent;
    ImageView btnback;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Orders");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_myorders);

        btnback=(ImageView)findViewById(R.id.btnback);
        parent=(LinearLayout)findViewById(R.id.myorders);

        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              parent.removeAllViews();

              for(DataSnapshot ds:dataSnapshot.getChildren()){
                  for(DataSnapshot dd:ds.getChildren()){
                      for(DataSnapshot d:dd.getChildren()){
                          if(d.getKey().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                              final String status=d.child("status").getValue().toString();
                              String oid=dd.getKey().toString();
                              final String path=ds.getKey().toString()+"/"+dd.getKey().toString();

                              final LinearLayout layout=new LinearLayout(provider_myorders.this);
                              LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 90);
                              params.setMargins(0,20,0,0);
                              layout.setLayoutParams(params);
                              layout.setOrientation(LinearLayout.HORIZONTAL);
                              layout.setBackground(ContextCompat.getDrawable(provider_myorders.this, R.drawable.t_stripe));

                              TextView service=new TextView(provider_myorders.this);
                              service.setText(oid);
                              service.setTextSize(18);
                              service.setGravity(Gravity.CENTER);
                              LinearLayout.LayoutParams para=new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1);
                              service.setLayoutParams(para);

                              final TextView order1=new TextView(provider_myorders.this);
                              order1.setText(status.toUpperCase());
                              order1.setTextSize(18);
                              order1.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.CENTER_VERTICAL);
                              order1.setTypeface(null, Typeface.BOLD);
                              order1.setBackground(ContextCompat.getDrawable(provider_myorders.this,R.drawable.commentborder));
                              LinearLayout.LayoutParams par=new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1);
                              order1.setLayoutParams(par);

                              layout.addView(service);
                              layout.addView(order1);
                              parent.addView(layout);

                              layout.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      if(status.equals("accepted")){
                                          Intent intent=new Intent(provider_myorders.this, provider_order_accepted.class);
                                          intent.putExtra("path",path);
                                          startActivity(intent);
                                      }
                                      if(status.equals("rejected")){
                                          Toast.makeText(provider_myorders.this, "This order was rejected", Toast.LENGTH_SHORT).show();
                                      }
                                      if(status.equals("completed")){
                                          Toast.makeText(provider_myorders.this, "This order has been completed", Toast.LENGTH_SHORT).show();
                                      }
                                  }
                              });


                          }
                      }
                  }
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
