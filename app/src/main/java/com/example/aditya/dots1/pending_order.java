package com.example.aditya.dots1;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class pending_order extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gmap;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Orders");
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    String oid, pid;
    TextView tvid,tvservice,tvlocation,tvtime,tvcost,tvcomment,tvheading,txtdistance,tvstatus;
    Button cancel;
    ImageView btnback;
    double lat,lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_order);

        oid=getIntent().getExtras().getString("oid");

        btnback=(ImageView)findViewById(R.id.btnback);
        tvid=(TextView)findViewById(R.id.tvid);
        tvservice=(TextView)findViewById(R.id.tvservice);
        tvlocation=(TextView)findViewById(R.id.tvlocation);
        tvtime=(TextView)findViewById(R.id.tvtime);
        tvcomment=(TextView)findViewById(R.id.tvcomment);
        tvcost=(TextView)findViewById(R.id.tvcost);
        cancel=(Button)findViewById(R.id.btnaccept);
        txtdistance=(TextView)findViewById(R.id.txtdistance);

        MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.gmap);
        mapFragment.getMapAsync(this);

        dbr.child(fauth.getCurrentUser().getUid()).child(oid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String serv=dataSnapshot.child("service").getValue().toString();
                String tim=dataSnapshot.child("time").getValue().toString();
                String loc=dataSnapshot.child("eaddress").getValue().toString();
                String comment=dataSnapshot.child("ecomment").getValue().toString();
                String cost="20$";
                lat= (double) dataSnapshot.child("latitude").getValue();
                lng= (double) dataSnapshot.child("longitude").getValue();

                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    int len=ds.getKey().toString().length();

                    if(len > 15){
                        String st=ds.child("status").getValue().toString();
                        if(st.equals("pending")){
                            pid=ds.getKey().toString();
                        }
                    }
                }

                tvid.setText(oid);
                tvservice.setText(serv);
                tvlocation.setText(loc);
                tvtime.setText(tim);
                tvcomment.setText(comment);
                tvcost.setText(cost);

                gmap.clear();
                final LatLng currentlocation=new LatLng(lat,lng);

                MarkerOptions markerOptions=new MarkerOptions();
                markerOptions.position(currentlocation);
                markerOptions.title("My Location");
                gmap.addMarker(markerOptions);
                gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,17.0f));
                    }
                });

                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,17.0f));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbr.child(fauth.getCurrentUser().getUid()).child(oid).child(pid).child("status").setValue("cancelled");
                Intent intent=new Intent(pending_order.this, myorders.class);
                startActivity(intent);
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap=googleMap;

    }
}
