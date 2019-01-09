package com.example.aditya.dots1;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
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
import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class newdrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    LinearLayout servicelist;
    Uri profile;
    Dialog dialog;
    String fname,email,activity="",piclink="",lname,cod="",orderpath="";
    Layout drawerhead;
    Button lo,lifting,plumbing,electric,btnasprovider;
    DatabaseReference dbr;
    DatabaseReference dbrorder=FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference dbrservices=FirebaseDatabase.getInstance().getReference("services");
    TextView tv;
    Boolean fbpic=false;
    FirebaseStorage storage=FirebaseStorage.getInstance();
    StorageReference storageReference=storage.getReferenceFromUrl("gs://dots-195d9.appspot.com");
    Button lines;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId("AVk4WFQZ8V_ct-PLjK5RrI1yAx6hq4Rt1pAKrPmNJkKAx3QOm1hDpQ-wBrrA-aGuhE7ZSmKZ9a9THHhN");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newdrawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        byte[] array=new byte[7];
        new Random().nextBytes(array);
        String generatedString=new String(array, Charset.forName("UTF-8"));

        servicelist=(LinearLayout)findViewById(R.id.service_layout);
        lines=(Button)findViewById(R.id.btnmenu);
        //btnasprovider=(Button)findViewById(R.id.btnasprovider);

        dbr= FirebaseDatabase.getInstance().getReference("Users");
        tv=(TextView)findViewById(R.id.tv);

        /*Intent in=new Intent(newdrawer.this, customer_notification_service.class);
        startService(in);
*/
        FirebaseUser currentuser=FirebaseAuth.getInstance().getCurrentUser();
        email=currentuser.getEmail();

        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fname=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("fname").getValue().toString();
                lname=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("lname").getValue().toString();
                tv.setText(fname);

                if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("profilepic")) {
                    piclink = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profilepic").getValue().toString();
                }

                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbrservices.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot ds:dataSnapshot.getChildren()){

                    float dip_mt=8f;
                    Resources r=getResources();
                    float px_mt= TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, dip_mt, r.getDisplayMetrics()
                    );

                    float dip_pl=20f;
                    float px_pl= TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, dip_pl, r.getDisplayMetrics()
                    );

                    float dip_h=35f;
                    float px_h= TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, dip_h, r.getDisplayMetrics()
                    );

                    float dip_ts=8f;
                    float px_ts= TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, dip_ts, r.getDisplayMetrics()
                    );

                    final LinearLayout layout = new LinearLayout(newdrawer.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) px_h);
                    params.setMargins(0, (int) px_mt, 0, 0);
                    layout.setLayoutParams(params);
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.setBackground(ContextCompat.getDrawable(newdrawer.this, R.drawable.t_stripe));

                    Button mybutton=new Button(newdrawer.this);
                    mybutton.setText(ds.getKey().toString());
                    mybutton.setTextSize(px_ts);
                    mybutton.setTypeface(null, Typeface.BOLD);
                    mybutton.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                    LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    mybutton.setLayoutParams(para);
                    mybutton.setPadding((int) px_pl,0,0,0);
                    mybutton.setBackground(ContextCompat.getDrawable(newdrawer.this, R.color.zxing_transparent));

                    layout.addView(mybutton);
                    servicelist.addView(layout);


                    mybutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(newdrawer.this,neworder.class);
                            intent.putExtra("service",ds.getKey().toString());
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
                                final String time=ds.child("stopwatch").child("time").getValue().toString();
                                final String cost=ds.child("cost").getValue().toString();
                                final String serv=ds.child("service").getValue().toString();
                                dialog=new Dialog(newdrawer.this);
                                dialog.setContentView(R.layout.job_done_notification);
                                dialog.show();
                                Button pay=dialog.findViewById(R.id.btnpay);
                                TextView timer=dialog.findViewById(R.id.tvTimer);

                                timer.setText(time);

                                pay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        beginpayment(cost, serv);
                                    }
                                });

                                    }
                                }
                                else {
                                    orderpath=FirebaseAuth.getInstance().getCurrentUser().getUid().toString()+"/"+ds.getKey().toString()+"/"+dd.getKey().toString();
                                    final String time=ds.child("stopwatch").child("time").getValue().toString();
                                    final String cost=ds.child("cost").getValue().toString();
                                    final String serv=ds.child("service").getValue().toString();
                                    dialog=new Dialog(newdrawer.this);
                                    dialog.setContentView(R.layout.job_done_notification);
                                    dialog.show();
                                    Button pay=dialog.findViewById(R.id.btnpay);
                                    TextView timer=dialog.findViewById(R.id.tvTimer);

                                    timer.setText(time);

                                    pay.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            beginpayment(cost, serv);
                                        }
                                    });
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



        /*QRGEncoder qrgEncoder=new QRGEncoder("Aditya Nath", null, QRGContents.Type.TEXT, 300);
        try{
            Bitmap bitmap=qrgEncoder.encodeAsBitmap();
            ImageView im=(ImageView)findViewById(R.id.testimage);
            im.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }*/




        storageReference.child("images/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profile=uri;
                    }
                });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header=navigationView.getHeaderView(0);
        Button btnap=(Button)header.findViewById(R.id.btnasprovider);

        btnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("current_status").setValue("provider");

                startActivity(new Intent(newdrawer.this, provider_home.class));
            }
        });
        //startService(new Intent(newdrawer.this,order_status_service.class));

        lines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            moveTaskToBack(true);

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

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(provider_home_service.MY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        Intent intent=new Intent(newdrawer.this, customer_notification_service.class);
        startService(intent);

        super.onStart();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_policy) {
            // Handle the camera action
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

        }else if(id ==R.id.lout){
            FirebaseAuth.getInstance().signOut();
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
                        Toast.makeText(this, "Payment was successfull", Toast.LENGTH_SHORT).show();
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
        super.onDestroy();
    }
}
