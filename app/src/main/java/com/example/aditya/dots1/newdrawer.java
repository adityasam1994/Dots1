package com.example.aditya.dots1;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class newdrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    LinearLayout servicelist;
    Uri profile;
    Dialog dialog;
    String fname,email,activity="",piclink="",lname,cod="",orderpath="", time,cost, serv, oid;
    Layout drawerhead;
    Button lo,lifting,plumbing,electric,btnasprovider;
    DatabaseReference dbr;
    DatabaseReference dbrorder=FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference dbrservices=FirebaseDatabase.getInstance().getReference("services");
    DatabaseReference dbruser = FirebaseDatabase.getInstance().getReference("Users");
    TextView tv;
    SharedPreferences sharedPreferences;
    Boolean fbpic=false;
    FirebaseStorage storage=FirebaseStorage.getInstance();
    StorageReference storageReference=storage.getReferenceFromUrl("gs://dots-195d9.appspot.com");
    Button lines;
    double u_lat, u_lng;
    TextView txtdistance;
    ImageView btnsetting;
    ListView listView;
    ArrayList<service> slist = new ArrayList<>();

    Intent mServiceIntent;
    private testcounterservice mYourService;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId("AVk4WFQZ8V_ct-PLjK5RrI1yAx6hq4Rt1pAKrPmNJkKAx3QOm1hDpQ-wBrrA-aGuhE7ZSmKZ9a9THHhN");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newdrawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialize(savedInstanceState);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header=navigationView.getHeaderView(0);
        final Button btnap=(Button)header.findViewById(R.id.btnasprovider);

        dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("status").getValue().toString().equals("provider")){
                    btnap.setVisibility(View.VISIBLE);
                }
                else {
                    btnap.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("current_status").setValue("provider");

                startActivity(new Intent(newdrawer.this, provider_home.class));
            }
        });
    }

    private void initialize(Bundle savedInstanceState) {
        listView=(ListView) findViewById(R.id.lv);
        btnsetting=(ImageView)findViewById(R.id.btnsetting);
        sharedPreferences=getSharedPreferences( "appopen", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("customer_at_home", true);
        editor.commit();
        servicelist=(LinearLayout)findViewById(R.id.service_layout);
        lines=(Button)findViewById(R.id.btnmenu);

        dbr= FirebaseDatabase.getInstance().getReference("Users");
        tv=(TextView)findViewById(R.id.tv);

        FirebaseUser currentuser=FirebaseAuth.getInstance().getCurrentUser();
        email=currentuser.getEmail();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            }
        }

        showmenu();
        showpayment();

        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("fname")) {
                    fname = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("fname").getValue().toString();
                }
                if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("fname")) {
                    lname = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("lname").getValue().toString();
                }
                tv.setText(fname);

                if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("profilepic")) {
                    piclink = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profilepic").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txtname = (TextView)view.findViewById(R.id.service);

                Intent intent=new Intent(newdrawer.this,neworder.class);
                intent.putExtra("service",txtname.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        storageReference.child("images/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profile=uri;
                    }
                });

        lines.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showpayment(){
        dbrorder.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    for(DataSnapshot dd:ds.getChildren()){
                        int le=dd.getKey().toString().length();
                        if(le > 15){
                            if(dd.child("status").getValue().toString().equals("completed")){

                                if(dd.hasChild("state")){
                                    if(!dd.child("state").getValue().toString().equals("approved")){
                                        orderpath=FirebaseAuth.getInstance().getCurrentUser().getUid().toString()+"/"+ds.getKey().toString()+"/"+dd.getKey().toString();

                                        time=ds.child("stopwatch").child("time").getValue().toString();
                                        cost=ds.child("cost").getValue().toString();
                                        serv=ds.child("service").getValue().toString();
                                        oid=ds.getKey().toString();

                                        show_dialogue();
                                    }

                                }
                                else {
                                    orderpath=FirebaseAuth.getInstance().getCurrentUser().getUid().toString()+"/"+ds.getKey().toString()+"/"+dd.getKey().toString();

                                    time=ds.child("stopwatch").child("time").getValue().toString();
                                    cost=ds.child("cost").getValue().toString();
                                    serv=ds.child("service").getValue().toString();
                                    oid=ds.getKey().toString();

                                    show_dialogue();

                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showmenu(){
        dbrservices.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot ds:dataSnapshot.getChildren()){

                    final double[] distance = {50000};
                    final int[] count = {0};
                    dbruser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("lati")) {
                                u_lat = (double) dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("lati").getValue();
                            }
                            if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("lati")) {
                                u_lng = (double) dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("longi").getValue();
                            }
                            for(DataSnapshot d : dataSnapshot.getChildren()) {
                                if (d.hasChild("status")) {
                                    if (d.child("status").getValue().toString().equals("provider")) {
                                        if (d.child("info").child("eservice").getValue().toString().equals(ds.getKey().toString())) {

                                            count[0] = count[0] + 1;

                                            double p_lat = (double) d.child("info").child("lati").getValue();
                                            double p_lng = (double) d.child("info").child("longi").getValue();

                                            Location locc = new Location("");
                                            locc.setLatitude(u_lat);
                                            locc.setLongitude(u_lng);

                                            Location locp = new Location("");
                                            locp.setLatitude(p_lat);
                                            locp.setLongitude(p_lng);

                                            double dist = (double) locc.distanceTo(locp);

                                            if (dist < distance[0]) {
                                                distance[0] = dist;
                                            }
                                        }
                                    }
                                }
                            }

                            double diss = distance[0] / 1000;
                            diss = diss * 100;
                            diss = Math.round(diss);
                            diss = diss / 100;

                            String dss = "";
                            if(diss < 50.0){
                                dss="("+diss+" Km"+")";
                            }
                            else {
                                dss="N/A";
                            }

                            final LinearLayout layout = new LinearLayout(newdrawer.this);

                            slist.add(new service(ds.getKey().toString(), dss));

                            servicelist.addView(layout);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Servicelistadapter adapter = new Servicelistadapter(this, R.layout.service_name, slist);
        listView.setAdapter(adapter);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }


    private void show_dialogue(){
        dialog=new Dialog(newdrawer.this);
        dialog.setContentView(R.layout.job_done_notification);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.HSVToColor(150,new float[]{0f,0f,93f})));
        dialog.show();

        Button pay=dialog.findViewById(R.id.btnpay);
        TextView timer=dialog.findViewById(R.id.tvTimer);
        TextView service=dialog.findViewById(R.id.tvservicename);
        TextView tvcost=dialog.findViewById(R.id.tvcost);

        service.setText("(" + oid+ ") " + serv + ":");
        tvcost.setText(cost+"$");

        timer.setText(time);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginpayment(cost, serv);
            }
        });
    }

    private void beginpayment(String cost, String serv) {
        Intent serviceConfig = new Intent(this, PayPalService.class);
        serviceConfig.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(serviceConfig);

        PayPalPayment payment = new PayPalPayment(new BigDecimal(cost),
                "USD", serv, PayPalPayment.PAYMENT_INTENT_SALE);

        Intent paymentConfig = new Intent(this, PaymentActivity.class);
        paymentConfig.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        paymentConfig.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(paymentConfig, 0);
    }

    private void generatecode() {
        final String ALPHA_NUM="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder=new StringBuilder();
        int count=7;
        while (count-- !=0){
            int charecter=(int)(Math.random()*ALPHA_NUM.length());
            builder.append(ALPHA_NUM.charAt(charecter));
            electric.setText(builder.toString());
            Toast.makeText(this, ""+builder, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if(isMyServiceRunning(customer_notification_service.class)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(newdrawer.this);
                builder.setTitle("Order in progress");
                builder.setMessage("If you exit the app, the order may get cancelled");
                builder.setPositiveButton("Exit anyway", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(newdrawer.this, "Okay", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
            else {
                moveTaskToBack(true);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.newdrawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {

        /*IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(provider_home_service.MY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);*/

        /*Intent intent=new Intent(newdrawer.this, customer_notification_service.class);
        startService(intent);*/

        super.onStart();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_policy) {
            startActivity(new Intent(newdrawer.this, Privacy_Policy.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(newdrawer.this,myorders.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_account) {
            startActivity(new Intent(newdrawer.this, myaccount.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else if (id == R.id.nav_report) {
            startActivity(new Intent(newdrawer.this, report_problem.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_contact) {

            startActivity(new Intent(newdrawer.this, Contact_us.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        }else if(id ==R.id.lout){
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
            Intent intent=new Intent(newdrawer.this,newlogin.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            cod=intent.getExtras().getString("orderid");

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK){
            PaymentConfirmation confirm = data.getParcelableExtra(
                    PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null){
                try {
                    Log.i("sampleapp", confirm.toJSONObject().toString(4));

                    JSONObject payment=confirm.toJSONObject();
                    JSONObject response=payment.getJSONObject("response");

                    String payment_id=response.getString("id");
                    String create_time=response.getString("create_time");
                    String state=response.getString("state");

                    if(state.equals("approved")) {
                        dbrorder.child(orderpath).child("payment_id").setValue(payment_id);
                        dbrorder.child(orderpath).child("create_time").setValue(create_time);
                        dbrorder.child(orderpath).child("state").setValue(state);

                        dialog.dismiss();
                        Intent intent=new Intent(newdrawer.this, rating.class);
                        intent.putExtra("orderpath", orderpath);
                        startActivity(intent);
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(this, "Payment was not approved!", Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    Log.e("sampleapp", "no confirmation data: ", e);
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("sampleapp", "The user canceled.");
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i("sampleapp", "Invalid payment / config set");
        }
    }


    @Override
    public void onDestroy(){
        stopService(new Intent(this, PayPalService.class));

        sharedPreferences=getSharedPreferences("appopen", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("customer_at_home", false);
        editor.commit();

        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnmenu:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                TextView name=(TextView)drawer.findViewById(R.id.tvname);
                ImageView image=(ImageView)drawer.findViewById(R.id.ivpic);
                TextView mail=(TextView)drawer.findViewById(R.id.tvemail);
                name.setText(fname+" "+lname);
                mail.setText(email);
                if(!piclink.equals("") && profile == null) {

                    Picasso.get().load(piclink).resize(100, 100).into(image);
                }
                if(profile != null){
                    Picasso.get().load(profile).resize(100, 100).into(image);
                }
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer((int)GravityCompat.START);
                }
                break;

        }
    }
}
