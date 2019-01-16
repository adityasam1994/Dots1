package com.example.aditya.dots1;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.aditya.dots1.App.CHANNEL_ID;

public class provider_home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    LinearLayout.LayoutParams params;
    private IntentIntegrator qrScan;
    Button bqrscan,btnstart,btncancel, btnascustomer;
    EditText etcode;
    LinearLayout qrlayout,detailayout;
    ImageView lines,imgplay,getdirection, btnplaynow;
    String fname="",emailid,cod,detail,tim,commen,cname="",caddress,cservice,uids,format,username,order_path,secretcode="",orderstatus="",customerid="";
    String nusername="",ntime="",ncommen="",naddress="",nservice="", nservicetype="",
            norderstatus="",ncod="",nformat="",nsercretcode="",ncustomerid="",norderpath="", nordertime="", current_order_path;
    double lat,lng, nlat=0,nlng=0;
    Uri filepath=null, videouri=null;
    FloatingActionButton fbclock;
    TextView tvname,tvcode,tvdetail,tvctime,tvccomment,tvcservice, tvservicetype, tvtimeremaining;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference dbrdetail=FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference dbruser=FirebaseDatabase.getInstance().getReference("Users");
    StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    StorageReference storagevideo= FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    SharedPreferences sharedPreferences;
    StorageReference strf= FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        stopService(new Intent(this, findprovider.class));
            /*final Context context=getBaseContext();
            Intent inten=new Intent(context,testsevice.class);
            inten.putExtra("receive", false);
            startService(inten);*/

        Intent intent=new Intent(provider_home.this, testsevice.class);
        PendingIntent pintent=PendingIntent.getService(provider_home.this, 0,intent,0);
        AlarmManager alarm=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30000, pintent);




        /*Intent intent = new Intent(provider_home.this, testsevice.class);
        startService(intent);*/

        sharedPreferences=getSharedPreferences( "appopen", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("provider_at_home", true);
        editor.commit();

        //startService(new Intent(provider_home.this,testsevice.class));
        qrlayout=(LinearLayout)findViewById(R.id.qrlayout);
        params= (LinearLayout.LayoutParams) qrlayout.getLayoutParams();

        fbclock=(FloatingActionButton)findViewById(R.id.fbtimer);
        tvtimeremaining=(TextView)findViewById(R.id.tvtimeremaining);
        pd=new ProgressDialog(this);
        btnplaynow=(ImageView)findViewById(R.id.imgplaynow);
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

        dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("order_in_progress")) {
                    current_order_path = dataSnapshot.child("order_in_progress").getValue().toString();
                    fbclock.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fbclock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(provider_home.this, timer.class);
                intent.putExtra("orderpath", current_order_path);
                startActivity(intent);
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

        /*imgplay.setOnClickListener(new View.OnClickListener() {
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
        });*/

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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(testsevice.MY_ACTIONS);
        registerReceiver(broadcastReceiver, intentFilter);
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
            nordertime=intent.getExtras().getString("ordertime");

            SimpleDateFormat forma=new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
            Date cd= Calendar.getInstance().getTime();
            final String ct=forma.format(cd);


            Date d1=null;
            Date d2=null;

            try {
                d1=forma.parse(nordertime);
                d2=forma.parse(ct);

                long millis=d1.getTime()+600000 - d2.getTime();
                long sec=millis/100%60;
                long mins=millis/(60*1000)%60;

                tvtimeremaining.setText("Will be rejected in: "+mins+" Mins");

            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(!ncod.equals("")) {

                if(nformat.equals("video")){
                    File mpath= Environment.getExternalStorageDirectory();

                    File dir=new File(mpath+"/Dot/");
                    dir.mkdirs();
                    String filename=ncod+".mp4";
                    File file=new File(dir, filename);

                    if(file.exists()){
                        btnplaynow.setVisibility(View.VISIBLE);
                        imgplay.setVisibility(View.INVISIBLE);
                    }
                }

                if(nformat.equals("image")){
                    File mpath= Environment.getExternalStorageDirectory();

                    File dir=new File(mpath+"/Dot/");
                    dir.mkdirs();
                    String filename=ncod+".jpg";
                    File file=new File(dir, filename);

                    if(file.exists()){
                        btnplaynow.setVisibility(View.VISIBLE);
                        imgplay.setVisibility(View.INVISIBLE);
                    }
                }

                imgplay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(nformat.equals("video")) {
                            pd.setMessage("Downloading...");
                            pd.show();
                            File mpath= Environment.getExternalStorageDirectory();

                            File dir=new File(mpath+"/Dot/");
                            dir.mkdirs();
                            String filename=ncod+".mp4";
                            File file=new File(dir, filename);

                            strf.child("order").child(ncustomerid).child(ncod).getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(provider_home.this, "Download complete", Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                    btnplaynow.setVisibility(View.VISIBLE);
                                    imgplay.setVisibility(View.INVISIBLE);
                                    playfile();
                                }
                            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                            .getTotalByteCount());
                                    pd.setMessage("Downloading "+(int)progress+"%");
                                }
                            });
                        }


                        if(nformat.equals("image")) {
                            pd.setMessage("Downloading...");
                            pd.show();
                            File mpath= Environment.getExternalStorageDirectory();

                            File dir=new File(mpath+"/Dot/");
                            dir.mkdirs();
                            String filename=ncod+".jpg";
                            File file=new File(dir, filename);

                            strf.child("order").child(ncustomerid).child(ncod).getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(provider_home.this, "Download complete", Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                    btnplaynow.setVisibility(View.VISIBLE);
                                    imgplay.setVisibility(View.INVISIBLE);
                                    playfile();
                                }
                            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                            .getTotalByteCount());
                                    pd.setMessage("Downloading "+(int)progress+"%");
                                }
                            });
                        }
                    }
                });

                btnplaynow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playfile();
                    }
                });



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

    private void playfile() {
        if(nformat.equals("video")){

            File mpath= Environment.getExternalStorageDirectory();

            File dir=new File(mpath+"/Dot/");
            dir.mkdirs();
            String filename=ncod+".mp4";
            File file=new File(dir, filename);

            Uri filepath=Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(filepath, "video/*");
            startActivity(Intent.createChooser(intent, "Open video Using"));
        }

        if(nformat.equals("image")){

            File mpath= Environment.getExternalStorageDirectory();

            File dir=new File(mpath+"/Dot/");
            dir.mkdirs();
            String filename=ncod+".jpg";
            File file=new File(dir, filename);

            Uri filepath=Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(filepath, "image/*");
            startActivity(Intent.createChooser(intent, "Open image Using"));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass){
        ActivityManager manager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        sharedPreferences=getSharedPreferences("appopen", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("provider_at_home", false);
        editor.commit();
    }
}
