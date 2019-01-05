package com.example.aditya.dots1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class provider_home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    LinearLayout.LayoutParams params;
    private IntentIntegrator qrScan;
    Button bqrscan,btnstart,btncancel, btnascustomer;
    EditText etcode;
    LinearLayout qrlayout,detailayout;
    ImageView lines,imgplay,getdirection;
    String fname="",emailid,cod,detail,tim,commen,cname="",caddress,cservice,uids,format,username,order_path,secretcode="",orderstatus="",customerid="";
    String nusername="",ntime="",ncommen="",naddress="",nservice="", nservicetype="",
            norderstatus="",ncod="",nformat="",nsercretcode="",ncustomerid="",norderpath="";
    double lat,lng, nlat=0,nlng=0;
    Uri filepath=null, videouri=null;
    TextView tvname,tvcode,tvdetail,tvctime,tvccomment,tvcservice, tvservicetype;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference dbrdetail=FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference dbruser=FirebaseDatabase.getInstance().getReference("Users");
    StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    StorageReference storagevideo= FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //startService(new Intent(provider_home.this,testsevice.class));
        qrlayout=(LinearLayout)findViewById(R.id.qrlayout);
        params= (LinearLayout.LayoutParams) qrlayout.getLayoutParams();

        //btnascustomer=(Button)findViewById(R.id.btnascustomer);
        tvservicetype=(TextView)findViewById(R.id.tvaccess);
        getdirection=(ImageView)findViewById(R.id.getdirection);
        detailayout=(LinearLayout)findViewById(R.id.details);
        btncancel=(Button)findViewById(R.id.btncancel);
        btnstart=(Button)findViewById(R.id.btnstart);
        imgplay=(ImageView)findViewById(R.id.imgplay);
        tvcservice=(TextView)findViewById(R.id.tvcservice);
        tvdetail=(TextView)findViewById(R.id.tvcdetail);
        tvctime=(TextView)findViewById(R.id.tvctime);
        tvccomment=(TextView)findViewById(R.id.tvccomment);
        etcode=(EditText)findViewById(R.id.etcode);
        bqrscan=(Button)findViewById(R.id.scanqr);
        lines=(ImageView)findViewById(R.id.lines);
        tvname=(TextView)findViewById(R.id.etname);
        tvcode=(TextView)findViewById(R.id.tvcode);

        detailayout.setVisibility(View.INVISIBLE);

        if(btnstart.getText().toString().equals("Accept")){
            qrlayout.setVisibility(View.INVISIBLE);
            params.height=0;

            btncancel.setVisibility(View.VISIBLE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header=navigationView.getHeaderView(0);
        Button btnap=(Button)header.findViewById(R.id.btnascustomer);

        btnap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("current_status").setValue("customer");

                startActivity(new Intent(provider_home.this, newdrawer.class));
            }
        });

        tvctime.setText(ntime);
        tvcservice.setText(nservice);
        tvcode.setText(ncod);
        tvccomment.setText(ncommen);
        tvservicetype.setText(nservicetype);

        cname=nusername+System.lineSeparator()+naddress;
        tvdetail.setText(cname);



        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fname=dataSnapshot.child(fauth.getCurrentUser().getUid()).child("fname").getValue().toString();
                tvname.setText(fname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        /*btnascustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbruser.child(fauth.getCurrentUser().getUid()).child("current_status").setValue("customer");

                startActivity(new Intent(provider_home.this, newdrawer.class));
            }
        });*/

        imgplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nformat.equals("video")) {
                    if (videouri != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(videouri, "video/*");
                        startActivity(Intent.createChooser(intent, "Play Video Using"));
                    }
                }
                if(nformat.equals("image")) {
                    if (videouri != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(videouri, "image/*");
                        startActivity(Intent.createChooser(intent, "Play Video Using"));
                    }
                }
            }
        });

        storageReference.child("images/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        filepath=uri;
                    }
                });

        lines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                TextView name=(TextView)drawer.findViewById(R.id.tvpname);
                TextView mail=(TextView)drawer.findViewById(R.id.tvpmail);
                ImageView pic=(ImageView)drawer.findViewById(R.id.ivppic);

                name.setText(fname);
                mail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                Picasso.get().load(filepath).resize(100,100).into(pic);

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer((int)GravityCompat.START);
                }
            }
        });


        bqrscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator=new IntentIntegrator(provider_home.this);
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.setPrompt("Scan the QR code");
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                intentIntegrator.initiateScan();
            }
        });

        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnstart.getText().toString().equals("Start")) {
                    if(etcode.getText().toString().equals(nsercretcode)){
                        Intent intent=new Intent(provider_home.this, timer.class);
                        intent.putExtra("orderpath",norderpath);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(provider_home.this, "Please enter a valid Code", Toast.LENGTH_SHORT).show();
                    }

                }

                if(btnstart.getText().toString().equals("Accept")){
                    //qrlayout.setVisibility(View.VISIBLE);
                    //params.height= LinearLayout.LayoutParams.WRAP_CONTENT;
                    //btnstart.setText("Start");
                    //btncancel.setVisibility(View.INVISIBLE);
                    //btncancel.setWidth(0);
                    //((ViewManager)btncancel.getParent()).removeView(btncancel);
                    try {
                        dbrdetail.child(norderpath).child(fauth.getCurrentUser().getUid()).child("status").setValue("accepted");

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbrdetail.child(norderpath).child(fauth.getCurrentUser().getUid()).child("status").setValue("cancelled");
            }
        });

        getdirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri="http://maps.google.com/maps?daddr="+
                        nlat+","+nlng+"("+"Customer"+")";
                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(provider_home_service.MY_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        Intent intent=new Intent(provider_home.this, provider_home_service.class);
        startService(intent);

        super.onStart();
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
        getMenuInflater().inflate(R.menu.provider_home, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_policy) {
            // Handle the camera action
        } else if (id == R.id.nav_account) {

            startActivity(new Intent(provider_home.this, myaccount.class));

        } else if (id == R.id.nav_orders) {

            Intent intent=new Intent(provider_home.this, provider_myorders.class);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_report) {

            startActivity(new Intent(provider_home.this, report_problem.class));

        }else if (id ==R.id.nav_logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent=new Intent(provider_home.this,newlogin.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(intentResult != null){
            if(intentResult.getContents() == null){
                Toast.makeText(this, "No code found", Toast.LENGTH_SHORT).show();
            }
            else {
                etcode.setText(intentResult.getContents());
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }



    }

    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ntime=intent.getExtras().getString("tim");
            nservice=intent.getExtras().getString("cservice");
            naddress=intent.getExtras().getString("caddress");
            ncommen=intent.getExtras().getString("commen");
            norderstatus=intent.getExtras().getString("orderstatus");
            ncod=intent.getExtras().getString("cod");
            nusername=intent.getExtras().getString("username");
            nformat=intent.getExtras().getString("format");
            nsercretcode=intent.getExtras().getString("secretcode");
            ncustomerid=intent.getExtras().getString("customerid");
            norderpath=intent.getExtras().getString("order_path");
            nservicetype=intent.getExtras().getString("servicetype");
            nlat=intent.getExtras().getDouble("lat");
            nlng=intent.getExtras().getDouble("lng");

            if(!ncod.equals("")) {

                detailayout.setVisibility(View.VISIBLE);

                tvctime.setText(ntime);
                tvcservice.setText(nservice);
                tvcode.setText(ncod);
                tvccomment.setText(ncommen);
                tvservicetype.setText(nservicetype);

                cname = nusername + System.lineSeparator() + naddress;
                tvdetail.setText(cname);

                storagevideo.child("order").child(ncustomerid).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                videouri = uri;
                            }
                        });

                if (!norderstatus.equals("") && norderstatus.equals("accepted")) {
                    btnstart.setText("Start");
                    qrlayout.setVisibility(View.VISIBLE);
                    params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, 0);
                    p.weight = 0;
                    btncancel.setLayoutParams(p);
                    //((ViewManager) btncancel.getParent()).removeView(btncancel);
                }
            }
            if(ncod.equals("")) {
                detailayout.setVisibility(View.INVISIBLE);
            }
        }
    };
}