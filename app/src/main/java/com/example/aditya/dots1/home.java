package com.example.aditya.dots1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;

public class home extends AppCompatActivity {

    Button lo,lifting,plumbing,electric;
    DatabaseReference dbr;
    TextView tv;
    FirebaseStorage storage=FirebaseStorage.getInstance();
    StorageReference storageReference=storage.getReferenceFromUrl("gs://dots-195d9.appspot.com");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        android.support.v7.widget.Toolbar toolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lifting=(Button)findViewById(R.id.btnlifting);
        plumbing=(Button)findViewById(R.id.btnplumbing);
        electric=(Button)findViewById(R.id.btnelectric);

        dbr= FirebaseDatabase.getInstance().getReference("Users");
        tv=(TextView)findViewById(R.id.tv);

        dbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fname=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("fname").getValue().toString();
                //String lname=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("lname").getValue().toString();
                tv.setText(fname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        lifting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(home.this,neworder.class);
                intent.putExtra("service","Lifting");
                startActivity(intent);
            }
        });
    }
}
