package com.example.aditya.dots1;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.Inflater;

import static android.view.MotionEvent.ACTION_DOWN;

public class statuspage extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

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
    String code,username, myservice, distance, result, uids;
    Intent mServiceIntent;
    Button btnsetmap, btnmapdone;
    Boolean editmap=false;
    double lat, lng;
    double distances;
    boolean killtimer=false, pending=false;
    CustomScrollView scrollView;
    List<String> rejected_providers=new ArrayList<>();

    private testcounterservice mYourService;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statuspage);

        btnmapdone=(Button)findViewById(R.id.btnmapdone);
        scrollView = (CustomScrollView) findViewById(R.id.scrollview);
        btnsetmap=(Button)findViewById(R.id.btnsetmap);
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

        lat =getIntent().getExtras().getDouble("lat");
        lng = getIntent().getExtras().getDouble("lng");

        myservice=getIntent().getExtras().getString("heading");
        tvid.setText(getIntent().getExtras().getString("code"));
        tvheading.setText(getIntent().getExtras().getString("heading"));
        tvservice.setText(getIntent().getExtras().getString("service"));
        tvlocation.setText(getIntent().getExtras().getString("location"));
        tvtime.setText(getIntent().getExtras().getString("time"));
        tvcomment.setText(getIntent().getExtras().getString("comment"));
        //txtdistance.setText(getIntent().getExtras().getString("distance"));
        tvcost.setText("20$");

        //scrollView.setEnableScrolling(false);

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

        btnsetmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editmap = true;
                scrollView.setEnableScrolling(false);
                btnsetmap.setVisibility(View.INVISIBLE);
                btnmapdone.setVisibility(View.VISIBLE);
            }
        });

        btnmapdone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editmap = false;
                scrollView.setEnableScrolling(true);
                btnmapdone.setVisibility(View.INVISIBLE);
                btnsetmap.setVisibility(View.VISIBLE);
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
                latitude = lat;
                longitude = lng;
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

                    editmap = false;
                    btnsetmap.setVisibility(View.INVISIBLE);
                    btnmapdone.setVisibility(View.INVISIBLE);
                //Toast.makeText(statuspage.this, ""+filePath, Toast.LENGTH_SHORT).show();

                StorageReference strf = storageReference.child("order/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).child(code);
                strf.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                pd.dismiss();
                                Toast.makeText(statuspage.this, "Order Placed.", Toast.LENGTH_SHORT).show();
                                Toast.makeText(statuspage.this, "Looking for provider, Don't close the app", Toast.LENGTH_SHORT).show();
                                accept.setText("Cancel");
                                tvheading.setText("Pending");
                                //findprovider();
                                final Context context=getBaseContext();
                                mServiceIntent = new Intent(context, customer_notification_service.class);
                                mServiceIntent.putExtra("tvservice", myservice);
                                mServiceIntent.putExtra("lat", lat);
                                mServiceIntent.putExtra("lng", lng);
                                mServiceIntent.putExtra("code", code);

                                btnmapdone.setVisibility(View.INVISIBLE);
                                btnmapdone.setVisibility(View.INVISIBLE);

                                /*IntentFilter intentFilter=new IntentFilter();
                                intentFilter.addAction(customer_notification_service.MY_ACTION);
                                registerReceiver(broadcastReceiver, intentFilter);*/


                                List<String> rej=new ArrayList<>();
                                Set<String> taskset = new HashSet<String>(rej);
                                //spref.edit().putStringSet("rej", taskset).commit();
                                spref.edit().remove("rej").commit();
                                spref.edit().putLong("StartTime", SystemClock.uptimeMillis()).commit();
                                spref.edit().putBoolean("accepted", false).commit();
                                spref.edit().putString("lat", "").commit();
                                spref.edit().putString("lng", "").commit();
                                spref.edit().putBoolean("cancelled", false).commit();
                                spref.edit().putBoolean("provider_at_home", false).commit();

                                check_status();
                                //startService(mServiceIntent);
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
            startActivity(new Intent(this, newdrawer.class));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap=googleMap;

        gmap.clear();
        final double lati,lngi;
        lati=getIntent().getExtras().getDouble("lat");
        lngi=getIntent().getExtras().getDouble("lng");
        final LatLng currentlocation=new LatLng(lati,lngi);

        final MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(currentlocation);
        markerOptions.title("I'm here");
        markerOptions.icon(bitmatdescriptorfromVector(getApplicationContext(),R.drawable.ic_iconfinder_pin));
        //markerOptions.draggable(true);
        gmap.addMarker(markerOptions);
        gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gmap.setBuildingsEnabled(true);

        CameraPosition cameraPosition = new CameraPosition.Builder().
                target(currentlocation)
                .tilt(45)
                .zoom(16)
                .build();

        gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        gmap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (editmap.equals(true)) {
                    gmap.clear();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("I'm here");
                    markerOptions.icon(bitmatdescriptorfromVector(getApplicationContext(),R.drawable.ic_iconfinder_pin));
                    gmap.addMarker(markerOptions);
                    gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                    CameraPosition cameraPosition = new CameraPosition.Builder().
                            target(latLng)
                            .tilt(45)
                            .zoom(16)
                            .build();

                    gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    lat = latLng.latitude;
                    lng = latLng.longitude;

                    Geocoder geo = new Geocoder(statuspage.this.getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        if (addresses.isEmpty()) {
                            Toast.makeText(statuspage.this, "Can't get this address", Toast.LENGTH_SHORT).show();
                        } else {
                            if (addresses.size() > 0) {
                                String ad = addresses.get(0).getAddressLine(0);
                                tvlocation.setText(ad);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private BitmapDescriptor bitmatdescriptorfromVector(Context applicationContext, int vector_res_id) {
        Drawable vectorDrawable = ContextCompat.getDrawable(applicationContext, vector_res_id);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //distance=intent.getExtras().getString("dist");
            //plat=intent.getExtras().getDouble("lat");
            //plng=intent.getExtras().getDouble("lng");

            distance=spref.getString("dist","50.0");
            plat= Double.parseDouble(spref.getString("lat","0.0"));
            plng= Double.parseDouble(spref.getString("lng","0.0"));

            txtdistance.setVisibility(View.VISIBLE);
            txtdistance.setText(distance);

            if(distance.equals("Not")){
                txtdistance.setText("Sorry! currently no provider available");
                accept.setVisibility(View.INVISIBLE);
            }

            gmap.clear();
            double lati,lngi;
            lati=lng;
            lngi=lng;

            final LatLng currentlocation=new LatLng(lat, lng);

            final LatLng cp=new LatLng(plat, plng);
            MarkerOptions mk2=new MarkerOptions();
            mk2.icon(bitmatdescriptorfromVector(getApplicationContext(),R.drawable.ic_iconfinder_pin_provider));
            mk2.position(cp);
            mk2.title("Provider");
            gmap.addMarker(mk2);

            MarkerOptions markerOptions=new MarkerOptions();
            markerOptions.position(currentlocation);
            markerOptions.icon(bitmatdescriptorfromVector(getApplicationContext(),R.drawable.ic_iconfinder_pin));
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

            if(plat != 0.0 && plng != 0.0) {
                gmap.animateCamera(cu);
            }
            else {
                gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,16.0f));
                unregisterReceiver(broadcastReceiver);
                stopService(new Intent(getBaseContext(), customer_notification_service.class));
            }


            /*gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    if(plat != 0 && plng != 0) {
                        gmap.animateCamera(cu);
                    }
                    else {
                        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation, 17.0f));
                    }
                }
            });*/
            /*Set<String> taskset = spref.getStringSet("rej", new HashSet<String>());
            List<String> tasklist = new ArrayList<>(taskset);
            Toast.makeText(context, ""+tasklist, Toast.LENGTH_SHORT).show();*/
        }
    };

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(mYourService, "Marker clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void search_provider() {

        distances=50000;
        uids=null;
        final int[] count = {0};
        plat=0;
        plng=0;

        dbruser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.hasChild("status")) {
                        if (ds.child("status").getValue().toString().equals("provider")) {
                            if (ds.child("info").child("eservice").getValue().toString().equals(myservice)) {

                                Set<String> taskset = spref.getStringSet("rej", new HashSet<String>());
                                List<String> tasklist = new ArrayList<>(taskset);

                                if (!tasklist.contains(ds.getKey().toString())) {

                                    plat = (double) ds.child("info").child("lati").getValue();
                                    plng = (double) ds.child("info").child("longi").getValue();

                                    double clat = lat;
                                    double clng = lng;

                                    Location locc = new Location("");
                                    locc.setLatitude(clat);
                                    locc.setLongitude(clng);

                                    Location locp = new Location("");
                                    locp.setLatitude(plat);
                                    locp.setLongitude(plng);

                                    double dist = locc.distanceTo(locp);

                                    if (dist < distances) {
                                        count[0] = count[0] +1;
                                        distances = dist;
                                        uids = ds.getKey().toString();
                                    }
                                }
                            }
                        }
                    }
                }
                SimpleDateFormat forma=new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                Date cd= Calendar.getInstance().getTime();
                final String dt=forma.format(cd);

                if(uids != null){
                    dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(code).child(uids).child("status").setValue("pending");
                    dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(code).child(uids).child("time").setValue(dt);
                    spref.edit().putLong("StartTime", SystemClock.uptimeMillis()).commit();
                    spref.edit().putString("uids", uids).commit();

                    double diss = distances/ 1000;
                    diss = diss * 100;
                    diss = Math.round(diss);
                    diss = diss / 100;
                    String d = "" + diss + " Km";
                    txtdistance.setVisibility(View.VISIBLE);
                    txtdistance.setText(d);

                    killtimer=false;
                    Timer();

                    check_status();
                    show_map();
                }
                else {
                    txtdistance.setVisibility(View.VISIBLE);
                    txtdistance.setText("Sorry! No provider was found");
                    accept.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private  void Timer(){
        final Handler handler=new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                String ui=spref.getString("uids", null);

                if((SystemClock.uptimeMillis() - spref.getLong("StartTime", SystemClock.uptimeMillis())) > 20000 && !killtimer
                        && !spref.getBoolean("accepted", false) && !spref.getBoolean("cancelled", false)){
                    String u=spref.getString("uids",null);
                    dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(code).child(u).child("status").setValue("rejected");
                    killtimer=true;
                }

                handler.postDelayed(this, 100);

                if(killtimer == true){
                    handler.removeCallbacks(this);
                }

            }
        });
    }

    private void check_status(){
        dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(code).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.getKey().toString().length() > 20){
                        String status=ds.child("status").getValue().toString();

                        if(status.equals("accepted")){
                            spref.edit().putBoolean("accepted", true).commit();
                            pending=true;
                            Intent intent=new Intent(statuspage.this, order_accepted.class);
                            intent.putExtra("pid", ds.getKey().toString());
                            intent.putExtra("oid", code);
                            intent.putExtra("lastpage", "statuspage");
                            startActivity(intent);
                            break;
                        }

                        else if(status.equals("cancelled")){
                            spref.edit().putBoolean("cancelled", true).commit();
                            Toast.makeText(statuspage.this, "Order Cancelled", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(statuspage.this, newdrawer.class);
                            pending=true;
                            startActivity(intent);
                            break;
                        }

                        else if(status.equals("rejected")){
                            rejected_providers.add(ds.getKey().toString());

                            Set<String> taskset = new HashSet<String>(rejected_providers);
                            spref.edit().putStringSet("rej", taskset).commit();

                            pending=false;
                        }

                        else if(status.equals("pending")){
                            pending=true;
                            break;
                        }
                    }
                }
                if(!pending){
                    search_provider();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void show_map(){
        gmap.clear();

        final LatLng currentlocation=new LatLng(lat, lng);

        final LatLng cp=new LatLng(plat, plng);
        MarkerOptions mk2=new MarkerOptions();
        mk2.icon(bitmatdescriptorfromVector(getApplicationContext(),R.drawable.ic_iconfinder_pin_provider));
        mk2.position(cp);
        mk2.title("Provider");
        gmap.addMarker(mk2);

        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(currentlocation);
        markerOptions.icon(bitmatdescriptorfromVector(getApplicationContext(),R.drawable.ic_iconfinder_pin));
        markerOptions.title("I'm here");
        gmap.addMarker(markerOptions);
        gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        builder.include(mk2.getPosition());
        builder.include(markerOptions.getPosition());

        LatLngBounds bounds=builder.build();

        int width=getResources().getDisplayMetrics().widthPixels;
        //int height=getResources().getDisplayMetrics().heightPixels;
        float dip_pl=200f;
        float px_pl= TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip_pl, getResources().getDisplayMetrics()
        );
        int height= (int) px_pl;
        int padding=(int)(height * 0.2);

        final CameraUpdate cu=CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        if(plat != 0) {
            gmap.animateCamera(cu);
        }
        else {
            gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentlocation,16.0f));
        }
    }
}
