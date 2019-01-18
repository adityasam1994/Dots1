package com.example.aditya.dots1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class order_accepted extends AppCompatActivity {

    public static final int REQUEST_CALL_PERMISSION = 846578;
    String pid,oid,lastpage="";
    TextView tvname,tvage,tvtime,secretcode;
    ImageView qrcode, btncall, btnback, providerpic;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference dbrorder=FirebaseDatabase.getInstance().getReference("Orders");
    StorageReference strf= FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_accepted);

        providerpic=(ImageView)findViewById(R.id.providerpic);
        btnback=(ImageView)findViewById(R.id.btnback);
        btncall=(ImageView) findViewById(R.id.btncall);
        secretcode=(TextView)findViewById(R.id.secretcode);
        qrcode=(ImageView)findViewById(R.id.qrcode);
        tvname=(TextView)findViewById(R.id.tvname);
        tvage=(TextView)findViewById(R.id.tvage);
        tvtime=(TextView)findViewById(R.id.tvtime);

        pid=getIntent().getExtras().getString("pid");
        oid=getIntent().getExtras().getString("oid");

        SimpleDateFormat format=new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        Date cd=Calendar.getInstance().getTime();
        String dt=format.format(cd);

        strf.child("images/"+(pid)).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(uri != null){
                            Picasso.get().load(uri).resize(200, 200).into(providerpic);
                        }

                    }
                });


        dbr.child(pid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fname=dataSnapshot.child("fname").getValue().toString();
                String age=dataSnapshot.child("info").child("eage").getValue().toString();
                String phone=dataSnapshot.child("ph").getValue().toString();

                if(dataSnapshot.hasChild("profilepic")){
                    String piclink=dataSnapshot.child("profilepic").getValue().toString();
                    Picasso.get().load(piclink).resize(200, 200).into(providerpic);
                }

                tvname.setText(fname);
                tvage.setText(age);
                tvtime.setText(phone);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getIntent() != null){
                    if(getIntent().getExtras().getString("lastpage").equals("myorders")){
                        finish();
                    }
                    if(getIntent().getExtras().getString("lastpage").equals("statuspage")){
                        startActivity(new Intent(order_accepted.this, newdrawer.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }
            }
        });

        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+tvtime.getText().toString().trim()));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
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



        dbrorder.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(oid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String qr=dataSnapshot.child("qrcode").getValue().toString();
                secretcode.setText(qr);

                QRGEncoder qrgEncoder=new QRGEncoder(qr, null, QRGContents.Type.TEXT, 300);
                try{
                    Bitmap bitmap=qrgEncoder.encodeAsBitmap();
                    qrcode.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CALL_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Call permission is required to make phone calls", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=getIntent();
        if(intent != null) {
            lastpage = getIntent().getExtras().getString("lastpage");
        }

        if(lastpage.equals("statuspage")) {
            startActivity(new Intent(order_accepted.this, newdrawer.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        if(lastpage.equals("myorders")){
            finish();
        }
    }
}
