package com.example.aditya.dots1;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.example.aditya.dots1.order_accepted.REQUEST_CALL_PERMISSION;

public class provider_order_accepted extends AppCompatActivity {

    String path, secretcode, nformat,code, phone;
    TextView tvservice,tvservicetype, tvtime, tvcode, tvuserdetail, tvcomment, tvid, tvname, etcode, txtphone;
    ImageView imgplay, getdiection, btnback, btnplaynow;
    double lat, lng;
    Uri videouri;
    ProgressDialog pd;
    Button btnstart,scanqr, btncall;
    DatabaseReference dbruser=FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Orders");
    StorageReference strf= FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_order_accepted);

        btncall=(Button)findViewById(R.id.btncall);
        txtphone=(TextView)findViewById(R.id.txtphone);
        btnplaynow=(ImageView)findViewById(R.id.imgplaynow);
        pd=new ProgressDialog(this);
        etcode=(EditText)findViewById(R.id.etcode);
        btnstart=(Button)findViewById(R.id.btnstart);
        scanqr=(Button)findViewById(R.id.scanqr);
        tvname=(TextView)findViewById(R.id.etname);
        tvservice=(TextView)findViewById(R.id.tvcservice);
        tvservicetype=(TextView)findViewById(R.id.tvaccess);
        tvtime=(TextView)findViewById(R.id.tvctime);
        tvcode=(TextView)findViewById(R.id.tvcode);
        tvuserdetail=(TextView)findViewById(R.id.tvcdetail);
        tvcomment=(TextView)findViewById(R.id.tvccomment);
        getdiection=(ImageView)findViewById(R.id.getdirection);
        imgplay=(ImageView) findViewById(R.id.imgplay);
        btnback=(ImageView)findViewById(R.id.lines);

        dbruser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tvname.setText(dataSnapshot.child("fname").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        path=getIntent().getExtras().getString("path");

        String uid=path.split("/")[0];

        dbruser.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ph=dataSnapshot.child("ph").getValue().toString();
                txtphone.setText(ph);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbr.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("username").getValue().toString();
                String address=dataSnapshot.child("eaddress").getValue().toString();
                String ecomment=dataSnapshot.child("ecomment").getValue().toString();
                String time=dataSnapshot.child("time").getValue().toString();
                String servicetype=dataSnapshot.child("servicetype").getValue().toString();
                String service=dataSnapshot.child("service").getValue().toString();
                secretcode=dataSnapshot.child("qrcode").getValue().toString();
                code=dataSnapshot.getKey().toString();
                nformat = dataSnapshot.child("format").getValue().toString();
                lat= (double) dataSnapshot.child("latitude").getValue();
                lng= (double) dataSnapshot.child("longitude").getValue();

                tvservice.setText(service);
                tvservicetype.setText(servicetype);
                tvtime.setText(time);
                tvcode.setText(code);
                tvcomment.setText(ecomment);

                String cname=name+System.lineSeparator()+address;

                tvuserdetail.setText(cname);

                if(nformat.equals("video")){
                    File mpath= Environment.getExternalStorageDirectory();
                    File dir=new File(mpath+"/Dots/received/");
                    dir.mkdirs();
                    String filename=code+".mp4";
                    File file=new File(dir, filename);

                    if(file.exists()){
                        btnplaynow.setVisibility(View.VISIBLE);
                        imgplay.setVisibility(View.INVISIBLE);
                    }
                }

                if(nformat.equals("image")){
                    File mpath= Environment.getExternalStorageDirectory();
                    File dir=new File(mpath+"/Dots/received/");
                    dir.mkdirs();
                    String filename=code+".jpg";
                    File file=new File(dir, filename);

                    if(file.exists()){
                        btnplaynow.setVisibility(View.VISIBLE);
                        imgplay.setVisibility(View.INVISIBLE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       /* strf.child("order").child(path).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        videouri=uri;
                        Toast.makeText(provider_order_accepted.this, ""+videouri, Toast.LENGTH_SHORT).show();
                    }
                });*/

        imgplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nformat.equals("video")) {
                        pd.setMessage("Downloading...");
                        pd.show();
                    File mpath= Environment.getExternalStorageDirectory();
                    File dir=new File(mpath+"/Dots/received/");
                    dir.mkdirs();
                    String filename=code+".mp4";
                    File file=new File(dir, filename);

                    strf.child("order").child(path).getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(provider_order_accepted.this, "Download complete", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                            btnplaynow.setVisibility(View.VISIBLE);
                            imgplay.setVisibility(View.INVISIBLE);
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            pd.setMessage("Downloading "+(int)progress+"%");
                        }
                    });

                        /*Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(videouri, "video/*");
                        startActivity(Intent.createChooser(intent, "Play Video Using"));*/
                }
                if(nformat.equals("image")) {

                        pd.setMessage("Downloading...");
                        pd.show();
                    File mpath= Environment.getExternalStorageDirectory();
                    File dir=new File(mpath+"/Dots/received/");
                    dir.mkdirs();
                    String filename=code+".jpg";
                    final File file=new File(dir, filename);

                        strf.child("order").child(path).getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(provider_order_accepted.this, "Download complete", Toast.LENGTH_SHORT).show();
                                btnplaynow.setVisibility(View.VISIBLE);
                                imgplay.setVisibility(View.INVISIBLE);
                                pd.dismiss();
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                        .getTotalByteCount());
                                pd.setMessage("Downloading "+(int)progress+"%");
                            }
                        });
                        /*Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(videouri, "image/*");
                        startActivity(Intent.createChooser(intent, "Open image Using"));*/
                }
            }
        });

        btnplaynow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nformat.equals("video")){

                    //File mpath= Environment.getExternalStorageDirectory();
                    File mpath= Environment.getExternalStorageDirectory();
                    File dir=new File(mpath+"/Dots/received/");
                    String filename=code+".mp4";
                    final File file=new File(dir, filename);

                    //Uri filepath=Uri.fromFile(file);
                    Uri filepath= FileProvider.getUriForFile(provider_order_accepted.this, BuildConfig.APPLICATION_ID + ".provider",file);

                    provider_order_accepted.this.grantUriPermission(provider_order_accepted.this.getPackageName(), filepath,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(filepath, "video/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Open video Using"));
                }

                if(nformat.equals("image")){

                    //File mpath= Environment.getExternalStorageDirectory();
                    File mpath= Environment.getExternalStorageDirectory();
                    File dir=new File(mpath+"/Dots/received/");
                    String filename=code+".jpg";
                    final File file=new File(dir, filename);

                    //Uri filepath=Uri.fromFile(file);
                    Uri filepath= FileProvider.getUriForFile(provider_order_accepted.this, BuildConfig.APPLICATION_ID + ".provider",file);
                    provider_order_accepted.this.grantUriPermission(provider_order_accepted.this.getPackageName(), filepath,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(filepath, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Open image Using"));
                }
            }
        });

        getdiection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*String uri="http://maps.google.com/maps?daddr="+
                        lat+","+lng+"("+"Customer"+")";
                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);*/
                Uri intenturi = Uri.parse("google.navigation:q=" + lat +"," +lng);
                Intent mapintent = new Intent(Intent.ACTION_VIEW, intenturi);
                mapintent.setPackage("com.google.android.apps.maps");
                startActivity(mapintent);
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        scanqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator=new IntentIntegrator(provider_order_accepted.this);
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
                if(etcode.getText().toString().equals(secretcode)){
                    dbr.child(path).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("in_progress");
                    Intent intent=new Intent(provider_order_accepted.this, timer.class);
                    intent.putExtra("orderpath",path);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(provider_order_accepted.this, "Please provide a valid code!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+txtphone.getText().toString().trim()));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                        startActivity(intent);
                    }
                    else {
                        String[] requestcall={Manifest.permission.CALL_PHONE};
                        requestPermissions(requestcall, REQUEST_CALL_PERMISSION);
                    }
                }
                else {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CALL_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Now you can call", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Call permission is required to make calls", Toast.LENGTH_SHORT).show();
            }
        }
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
}
