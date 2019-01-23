package com.example.aditya.dots1;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class report_problem extends AppCompatActivity {

    Button submit;
    ImageView btnback;
    EditText message;
    DatabaseReference dbruser= FirebaseDatabase.getInstance().getReference("Users");
    DatabaseReference dbr=FirebaseDatabase.getInstance().getReference("messages");
    FirebaseAuth fauth=FirebaseAuth.getInstance();
    String userid, mailid, mail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        btnback=(ImageView)findViewById(R.id.btnback);
        message=(EditText)findViewById(R.id.etmessage);
        submit=(Button)findViewById(R.id.btnsubmit);

        userid=dbr.child(fauth.getCurrentUser().getUid()).getKey().toString();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int[] count = new int[1];
                dbr.child(fauth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        count[0] = (int) dataSnapshot.getChildrenCount();
                        int newnumber=count[0]+1;
                        dbr.child(fauth.getCurrentUser().getUid()).child(String.valueOf(newnumber)).child("message").setValue(mail);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                mail=message.getText().toString();
                Intent emailIntent=new Intent(Intent.ACTION_SEND);
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                emailIntent.setType("plain/text");
                emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"adityanathtiwari25@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Test Message from dots");
                emailIntent.putExtra(Intent.EXTRA_TEXT, mail);
                startActivity(emailIntent);

            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
