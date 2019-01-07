package com.example.aditya.dots1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class select extends AppCompatActivity {

    Button provider,customer;
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    ProgressDialog pd;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        provider=(Button)findViewById(R.id.btnprovider);
        customer=(Button)findViewById(R.id.btncustomer);

        pd=new ProgressDialog(this);
        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Saving...");
                pd.show();
                dbr.child(fauth.getCurrentUser().getUid()).child("status").setValue("customer")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Intent intent=new Intent(select.this, newdrawer.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(select.this, "Falied to save "+e, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        provider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Saving...");
                pd.show();
                dbr.child(fauth.getCurrentUser().getUid()).child("status").setValue("provider")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        startActivity(new Intent(select.this,provider_detail.class));
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(select.this, "Failed to save "+e, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }
}
