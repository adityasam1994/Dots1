package com.example.aditya.dots1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class password_reset extends AppCompatActivity {

    EditText etmail;
    Button btnsend;
    ProgressDialog pd;
    ImageView btnback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        btnback=(ImageView)findViewById(R.id.btnback);
        btnsend=(Button)findViewById(R.id.btnsend);
        etmail=(EditText)findViewById(R.id.etmail);
        pd=new ProgressDialog(this);
        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Sending password rest link...");
                pd.show();
                String email=etmail.getText().toString();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    pd.dismiss();
                                    Toast.makeText(password_reset.this,"Check the email to reset the password",Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(password_reset.this,newlogin.class);
                                    startActivity(intent);
                                }
                                else {
                                    pd.dismiss();
                                    Toast.makeText(password_reset.this,"Failed to send the password rest link!",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
