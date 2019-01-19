package com.example.aditya.dots1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class statuspage extends AppCompatActivity implements OnMapReadyCallback{

    GoogleMap gmap;
    TextView tvid,tvservice,tvlocation,tvtime,tvcost,tvcomment,tvheading,txtdistance,tvstatus, loc_lab, com_lab;
    Button accept;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    DatabaseReference dbr;
    DatabaseReference dbruser;
    StorageReference storageReference;
    Uri filePath;
    SharedPreferences spref;
    SharedPreferences sharedPreferences;
    ProgressDialog pd;
    ImageView btnback;
    double plat=0, plng=0;
    FrameLayout frameLayout;
    String code,username, myservice, distance, result;
    Intent mServiceIntent;
    private testcounterservice mYourService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statuspage);

        frameLayout=(FrameLayout)findViewById(R.id.frameLayout);
        sharedPreferences=getSharedPreferences("cnotification", Context.MODE_PRIVATE);
        spref=getSharedPreferences("appopen",Context.MODE_PRIVATE);
        pd=new ProgressDialog(this);
        tvid=(TextView)findViewById(R.id.tvid);
        tvservice=(TextView)findViewById(R.id.tvservice);
        tvlocation=(TextView)findViewById(R.id.tvlocation);
        tvtime=(TextView)findViewById(R.id.tvtime);
        tvcomment=(TextView)findViewById(R.id.tvcomment);
        tvcost=(TextView)findViewById(R.id.tvcost);
        tvheading=(TextView)findViewById(R.id.tvheading);
        accept=(Button)findViewById(R.id.btnaccept);
        txtdistance=(TextView)findViewById(R.id.txtdistance);
        btnback=(ImageView)findViewById(R.id.btnback);
        loc_lab=(TextView)findViewById(R.id.loc_lab);
        com_lab=(TextView)findViewById(R.id.com_lab);

        myservice=getIntent().getExtras().getString("heading");
        tvid.setText(getIntent().getExtras().getString("code"));
        tvheading.setText(getIntent().getExtras().getString("heading"));
        tvservice.setText(getIntent().getExtras().getString("service"));
        tvlocation.setText(getIntent().getExtras().getString("location"));
        tvtime.setText(getIntent().getExtras().getString("time"));
        tvcomment.setText(getIntent().getExtras().getString("comment"));
        //txtdistance.setText(getIntent().getExtras().getString("distance"));
        tvcost.setText("20$");

        final ViewTreeObserver frameobserver=frameLayout.getViewTreeObserver();
        frameobserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width=frameLayout.getWidth();
                LinearLayout.LayoutParams ppframe= (LinearLayout.LayoutParams) ((FrameLayout)findViewById(R.id.frameLayout)).getLayoutParams();
                ppframe.height=width;
                frameLayout.setLayoutParams(ppframe);
            }
        });

        filePath=getIntent().getData();

        dbr=FirebaseDatabase.getInstance().getReference("Orders");
        dbruser=FirebaseDatabase.getInstance().getReference("Users");
        storageReference=firebaseStorage.getReferenceFromUrl("gs://dots-195d9.appspot.com");

        dbruser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                username=dataSnapshot.child(fauth.getCurrentUser().getUid()).child("fname").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final ViewTreeObserver observer=tvlocation.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height=tvlocation.getHeight();
                LinearLayout.LayoutParams pp= (LinearLayout.LayoutParams) ((TextView)findViewById(R.id.loc_lab)).getLayoutParams();
                pp.height=height;
                loc_lab.setLayoutParams(pp);
            }
        });

        final ViewTreeObserver cobserver=tvlocation.getViewTreeObserver();
        cobserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height=tvcomment.getHeight();
                LinearLayout.LayoutParams pp= (LinearLayout.LayoutParams) ((TextView)findViewById(R.id.com_lab)).getLayoutParams();
                pp.height=height;
                com_lab.setLayoutParams(pp);
            }
        });

        MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.gmap);
        mapFragment.getMapAsync(this);

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accept.getText().toString().equals("Cancel")){
                    startActivity(new Intent(statuspage.this, newdrawer.class));
                }
                else {
                    finish();
                }
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(accept.getText().toString().equals("Cancel")){
                    dbr.child(fauth.getCurrentUser().getUid()).child(getIntent().getExtras().getString("code")).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds:dataSnapshot.getChildren()){
                                int len=ds.getKey().toString().length();

                                if(len>15){
                                    String pis=ds.getKey().toString();
                                    if(ds.child("status").getValue().toString().equals("pending")){
                                        dbr.child(fauth.getCurrentUser().getUid()).child(getIntent().getExtras().getString("code"))
                                                .child(pis).child("status").setValue("cancelled");
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                if(accept.getText().toString().equals("Accept"))
                {
                pd.setMessage("Placing order...");
                pd.setCancelable(false);
                pd.show();
                final String service, time, ecomment, eaddress, format, codeforqr, servicetype;
                double latitude, longitude;
                codeforqr = getIntent().getExtras().getString("codeforqr");
                servicetype = tvservice.getText().toString();
                service = tvheading.getText().toString();
                time = tvtime.getText().toString();
                ecomment = tvcomment.getText().toString();
                eaddress = tvlocation.getText().toString();
                latitude = getIntent().getExtras().getDouble("lat");
                longitude = getIntent().getExtras().getDouble("lng");
                String cost="20";

                code = getIntent().getExtras().getString("code");
                format = getIntent().getExtras().getString("format");

                    SimpleDateFormat forma=new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                    Date cd= Calendar.getInstance().getTime();
                    final String dt=forma.format(cd);

                order o = new order(service, time, ecomment, eaddress, latitude, longitude, code, format, username, servicetype, cost);
                dbr.child(fauth.getCurrentUser().getUid()).child(code).setValue(o)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dbr.child(fauth.getCurrentUser().getUid()).child(code).child("qrcode").setValue(codeforqr);
                        dbr.child(fauth.getCurrentUser().getUid()).child(code).child("Date").setValue(dt);
                    }
                });


                //Toast.makeText(statuspage.this, ""+filePath, Toast.LENGTH_SHORT).show();

                StorageReference strf = storageReference.child("order/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).child(code);
                strf.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                pd.dismiss();
                                Toast.makeText(statuspage.this, "Order Placed.", Toast.LENGTH_SHORT).show();
                                accept.setText("Cancel");
                                tvheading.setText("Pending");
                                //findprovider();
                                final Context context=getBaseContext();
                                mServiceIntent = new Intent(context, customer_notification_service.class);
                                mServiceIntent.putExtra("tvservice", myservice);
                                mServiceIntent.putExtra("lat", getIntent().getExtras().getDouble("lat"));
                                mServiceIntent.putExtra("lng", getIntent().getExtras().getDouble("lng"));
                                mServiceIntent.putExtra("code", code);

                                IntentFilter intentFilter=new IntentFilter();
                                intentFilter.addAction(customer_notification_service.MY_ACTION);
                                registerReceiver(broadcastReceiver, intentFilter);

                                List<String> rej=new ArrayList<>();
                                Set<String> taskset = new HashSet<String>(rej);
                                spref.edit().putStringSet("rej", taskset).commit();
                                spref.edit().putLong("StartTime", SystemClock.uptimeMillis()).commit();
                                spref.edit().putBoolean("accepted", false).commit();
                                spref.edit().putBoolean("cancelled", false).commit();

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(mServiceIntent );
                                }
                                else {
                                    startService(mServiceIntent);
                                }
                            }
                        })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        pd.setMessage("Placing order... "+(int)progress+"%");
                    }
                });

            }
        }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(accept.getText().toString().equals("Cancel")){
            startActivity(new Intent(statuspage.this,newdrawer.class));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap=googleMap;

        gmap.clear();
        double lat,lng;
        lat=getIntent().getExtras().getDouble("lat");
        lng=getIntent().getExtras().getDouble("lng");
        final LatLng currentlocation=new LatLng(lat,lng);

        //plat=Double.longBitsToDouble(spref.getLong("lat", 0));
        //plng=Double.longBitsToDouble(spref.getLong("lng", 0));

        /*final LatLng cp=new LatLng(plat, plng);
        MarkerOptions mk2=new MarkerOptions();
        mk2.position(cp);
        mk2.title("Provider");
        gmap.addMarker(mk2);*/

        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(currentlocation);
        markerOptions.title("I'm here");
        gmap.addMarker(markerOptions);
        gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        /*LatLngBounds.Builder builder=new LatLngBounds.Builder();
        builder.include(mk2.getPosition());
        builder.include(markerOptions.getPosition());

        LatLngBounds bounds=builder.build();

        int width=getResources().getDisplayMetrics().widthPixels;
        int height=getResources().getDisplayMetrics().heightPixels;
        int padding=(int)(width * 0.2);
*/
  //      final CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,17.0f));

        gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 17.0f));
            }
        });
    }

    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            distance=intent.getExtras().getString("dist");
            plat=intent.getExtras().getDouble("lat");
            plng=intent.getExtras().getDouble("lng");

            txtdistance.setVisibility(View.VISIBLE);
            txtdistance.setText(distance);

            if(distance.equals("Not found")){
                txtdistance.setText("Sorry! currently no provider available");
                accept.setVisibility(View.INVISIBLE);
            }

            gmap.clear();
            double lat,lng;
            lat=getIntent().getExtras().getDouble("lat");
            lng=getIntent().getExtras().getDouble("lng");
            final LatLng currentlocation=new LatLng(lat,lng);

            final LatLng cp=new LatLng(plat, plng);
            MarkerOptions mk2=new MarkerOptions();
            mk2.position(cp);
            mk2.title("Provider");
            gmap.addMarker(mk2);

            MarkerOptions markerOptions=new MarkerOptions();
            markerOptions.position(currentlocation);
            markerOptions.title("I'm here");
            gmap.addMarker(markerOptions);
            gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            LatLngBounds.Builder builder=new LatLngBounds.Builder();
            builder.include(mk2.getPosition());
            builder.include(markerOptions.getPosition());

            LatLngBounds bounds=builder.build();

            int width=getResources().getDisplayMetrics().widthPixels;
            int height=getResources().getDisplayMetrics().heightPixels;
            int padding=(int)(height * 0.2);

            final CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            if(plat != 0 && plng != 0) {
                gmap.animateCamera(cu);
            }
            else {
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,17.0f));
            }

            gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    if(plat != 0 && plng != 0) {
                        gmap.animateCamera(cu);
                    }
                    else {
                        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 17.0f));
                    }
                }
            });
        }
    };
}
