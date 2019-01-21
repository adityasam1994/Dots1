package com.example.aditya.dots1;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
    DatabaseReference dbruser=FirebaseDatabase.getInstance().getReference("Users");
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    String oid, pid;
    TextView tvid,tvservice,tvlocation,tvtime,tvcost,tvcomment,tvheading,txtdistance,tvstatus, location_label,comment_label;
    Button cancel, btngmap;
    ImageView btnback;
    double lat,lng, plat, plng, distance;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_order);

        oid=getIntent().getExtras().getString("oid");

        btngmap=(Button)findViewById(R.id.btngmap);
        frameLayout=(FrameLayout)findViewById(R.id.frameLayout);
        btnback=(ImageView)findViewById(R.id.btnback);
        tvid=(TextView)findViewById(R.id.tvid);
        tvservice=(TextView)findViewById(R.id.tvservice);
        tvlocation=(TextView)findViewById(R.id.tvlocation);
        tvtime=(TextView)findViewById(R.id.tvtime);
        tvcomment=(TextView)findViewById(R.id.tvcomment);
        tvcost=(TextView)findViewById(R.id.tvcost);
        cancel=(Button)findViewById(R.id.btnaccept);
        txtdistance=(TextView)findViewById(R.id.txtdistance);
        location_label=(TextView)findViewById(R.id.location_lable);
        comment_label=(TextView)findViewById(R.id.comment_label);


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


                final ViewTreeObserver observer=tvlocation.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int height=tvlocation.getHeight();
                        LinearLayout.LayoutParams pp= (LinearLayout.LayoutParams) ((TextView)findViewById(R.id.location_lable)).getLayoutParams();
                        pp.height=height;
                        location_label.setLayoutParams(pp);
                    }
                });

                final ViewTreeObserver cobserver=tvlocation.getViewTreeObserver();
                cobserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int height=tvcomment.getHeight();
                        LinearLayout.LayoutParams pp= (LinearLayout.LayoutParams) ((TextView)findViewById(R.id.comment_label)).getLayoutParams();
                        pp.height=height;
                        comment_label.setLayoutParams(pp);
                    }
                });

                dbruser.child(pid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("info").hasChild("lati")){
                            plat= (double) dataSnapshot.child("info").child("lati").getValue();
                            plng= (double) dataSnapshot.child("info").child("longi").getValue();
                        }
                        else {
                            plat = (double) dataSnapshot.child("lati").getValue();
                            plng = (double) dataSnapshot.child("longi").getValue();
                        }

                        gmap.clear();
                        final LatLng currentlocation=new LatLng(lat,lng);
                        final LatLng providerlocation=new LatLng(plat,plng);

                        MarkerOptions markerOptions=new MarkerOptions();
                        markerOptions.position(currentlocation);
                        markerOptions.title("My Location");
                        gmap.addMarker(markerOptions);

                        MarkerOptions provider=new MarkerOptions();
                        provider.position(providerlocation);
                        provider.title("Provider");
                        gmap.addMarker(provider);

                        Location locc=new Location("");
                        locc.setLatitude(lat);
                        locc.setLongitude(lng);

                        Location locp=new Location("");
                        locp.setLatitude(plat);
                        locp.setLongitude(plng);

                        distance=locc.distanceTo(locp)/1000;

                        txtdistance.setText((int) distance+" Km");

                        LatLngBounds.Builder builder=new LatLngBounds.Builder();
                        builder.include(provider.getPosition());
                        builder.include(markerOptions.getPosition());
                        LatLngBounds bounds=builder.build();

                        int width=getResources().getDisplayMetrics().widthPixels;
                        int height=getResources().getDisplayMetrics().heightPixels;
                        int padding=(int)(height * 0.2);

                        final CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                        gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                            @Override
                            public void onCameraIdle() {
                                gmap.animateCamera(cu);
                                //gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,17.0f));
                            }
                        });

                        gmap.animateCamera(cu);
                        //gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,17.0f));
                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btngmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(pending_order.this, "It's working", Toast.LENGTH_SHORT).show();
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
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap=googleMap;

    }
}
