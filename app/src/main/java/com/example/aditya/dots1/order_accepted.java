package com.example.aditya.dots1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.WriterException;

import org.w3c.dom.Text;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class order_accepted extends AppCompatActivity {

    String pid,oid,lastpage="";
    TextView tvname,tvage,tvtime,secretcode;
    ImageView qrcode;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference dbrorder=FirebaseDatabase.getInstance().getReference("Orders");
    StorageReference strf= FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_accepted);

        secretcode=(TextView)findViewById(R.id.secretcode);
        qrcode=(ImageView)findViewById(R.id.qrcode);
        tvname=(TextView)findViewById(R.id.tvname);
        tvage=(TextView)findViewById(R.id.tvage);
        tvtime=(TextView)findViewById(R.id.tvtime);

        pid=getIntent().getExtras().getString("pid");
        oid=getIntent().getExtras().getString("oid");

        dbr.child(pid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fname=dataSnapshot.child("fname").getValue().toString();
                String age=dataSnapshot.child("info").child("eage").getValue().toString();
                String phone=dataSnapshot.child("ph").getValue().toString();

                tvname.setText(fname);
                tvage.setText(age);
                tvtime.setText(phone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
    public void onBackPressed() {
        super.onBackPressed();
        lastpage=getIntent().getExtras().getString("lastpage");

        if(lastpage.equals("neworder")) {
            startActivity(new Intent(order_accepted.this, newdrawer.class));
        }
    }
}
