package com.example.aditya.dots1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class newlogin extends AppCompatActivity {

    Button su,btnli,btnreset,btnimage;
    EditText etmail,etpass;
    FirebaseAuth fauth;
    ProgressDialog pd;
    CallbackManager callbackManager;
    DatabaseReference dbr= FirebaseDatabase.getInstance().getReference("Users");
    String status;
    LoginButton fblogin;
    ImageButton fbimagebutton, gimagebutton;
    RequestQueue requestQueue;
    String name,lname,fname;
    String[] namewords;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN=101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newlogin);

        fauth=FirebaseAuth.getInstance();
        callbackManager=CallbackManager.Factory.create();
        pd=new ProgressDialog(this);
        btnreset=(Button)findViewById(R.id.btnreset);

        su=(Button)findViewById(R.id.btnsignup);
        btnli=(Button)findViewById(R.id.btnstart);
        btnimage=(Button)findViewById(R.id.btnimage);
        etmail=(EditText)findViewById(R.id.etmail);
        etpass=(EditText)findViewById(R.id.etpass);

        su.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openativity2();
            }
        });

        btnli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginuser();
            }
        });

        btnreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset_password();
            }
        });

        btnimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(newlogin.this,newdrawer.class);
                startActivity(intent);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        fblogin=(LoginButton)findViewById(R.id.fsignin);
        fbimagebutton=(ImageButton)findViewById(R.id.ibfb);
        gimagebutton=(ImageButton)findViewById(R.id.ibgoogle);

        requestQueue = Volley.newRequestQueue(this);
        callbackManager = CallbackManager.Factory.create();
        fblogin.setReadPermissions("email", "public_profile");
        fblogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                pd.setMessage("Logging in...");
                pd.show();
                handleaccesstoken(loginResult.getAccessToken(), name);
            }

            @Override
            public void onCancel() {
                Toast.makeText(newlogin.this, "Canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(newlogin.this, "Oop! Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });


        gimagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Logging in...");
                pd.show();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void handleaccesstoken(final AccessToken token, final String name){
        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());
        fauth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            Boolean isnewuser=task.getResult().getAdditionalUserInfo().isNewUser();

                            if(isnewuser){

                                String name=FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString();
                                String photo=fauth.getCurrentUser().getPhotoUrl().toString();
                                String phot=photo+"?height=500";

                                String[] splitname=getnamewords(name);
                                if(splitname.length >1){
                                    lname=splitname[splitname.length-1];
                                    fname=name.replace(lname,"").trim();
                                }

                                gsignin gsign=new gsignin(fname,lname);
                                dbr.child(fauth.getCurrentUser().getUid()).setValue(gsign);
                                dbr.child(fauth.getCurrentUser().getUid()).child("profilepic").setValue(phot);

                                pd.dismiss();

                                Intent intent = new Intent(newlogin.this, select.class);
                                startActivity(intent);

                            /*StorageReference sref = storageRef.child("images/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
                            Uri file=Uri.parse(photo);
                            sref.putFile(file);

                            if(filePath != null) {
                                Uri uri=Uri.parse("android:resource://com.example.aditya.dots1/drawable/camera");
                                sref.putFile(uri);
                                ivprofile.setImageURI(uri);
                            }else {
                                Uri uri=Uri.parse("android:resource://com.example.aditya.dots1/drawable/cam");
                                sref.putFile(uri);
                                ivprofile.setImageURI(uri);
                            }*/

                            }
                            else {
                                dbr.child(fauth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String st=dataSnapshot.child("status").getValue().toString();

                                        if(status.equals("customer")) {
                                            if(dataSnapshot.hasChild("current_status")){
                                                if(dataSnapshot.child("current_status").getValue().toString().equals("customer")){
                                                    startActivity(new Intent(newlogin.this, newdrawer.class));
                                                }
                                                if(dataSnapshot.child("current_status").getValue().toString().equals("provider")){
                                                    startActivity(new Intent(newlogin.this, provider_home.class));
                                                }
                                            }
                                            else {
                                                startActivity(new Intent(newlogin.this, newdrawer.class));
                                            }
                                        }
                                        if(status.equals("provider")){
                                            if(dataSnapshot.hasChild("current_status")){
                                                if(dataSnapshot.child("current_status").getValue().toString().equals("customer")){
                                                    startActivity(new Intent(newlogin.this, newdrawer.class));
                                                }
                                                if(dataSnapshot.child("current_status").getValue().toString().equals("provider")){
                                                    startActivity(new Intent(newlogin.this, provider_home.class));
                                                }
                                            }
                                            else {
                                                startActivity(new Intent(newlogin.this, provider_home.class));
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }


                        }else {
                            Toast.makeText(newlogin.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String[] getnamewords(String name) {
        if(name != null){
            namewords=name.split("\\s+");

        }
        return namewords;
    }

    public void openativity2(){
        Intent intent=new Intent(this, newsignup.class);
        startActivity(intent);
    }

    public void onClickfb(View view){
        if(view == fbimagebutton){
            fblogin.performClick();
        }
    }


    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        fauth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {

                Boolean isnewuser=task.getResult().getAdditionalUserInfo().isNewUser();

                if(isnewuser){

                FirebaseUser user = fauth.getCurrentUser();
                String name=account.getDisplayName();
                Uri photouri=(account.getPhotoUrl());
                String photstring=photouri.toString();

                String[] splitname=getnamewords(name);
                if(splitname.length>1){
                    lname=splitname[splitname.length-1];
                    fname=name.replace(lname,"").trim();
                }

                gsignin gsign=new gsignin(fname,lname);
                dbr.child(user.getUid()).setValue(gsign);
                dbr.child(user.getUid()).child("profilepic").setValue(photstring);

                pd.dismiss();

                Intent intent = new Intent(newlogin.this, select.class);
                startActivity(intent);

                            /*StorageReference childRef = storageRef.child("images/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                            childRef.putFile(photouri);*/

            }

            else {

                    dbr.child(fauth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String st=dataSnapshot.child("status").getValue().toString();

                            if(status.equals("customer")) {
                                if(dataSnapshot.hasChild("current_status")){
                                    if(dataSnapshot.child("current_status").getValue().toString().equals("customer")){
                                        startActivity(new Intent(newlogin.this, newdrawer.class));
                                    }
                                    if(dataSnapshot.child("current_status").getValue().toString().equals("provider")){
                                        startActivity(new Intent(newlogin.this, provider_home.class));
                                    }
                                }
                                else {
                                    startActivity(new Intent(newlogin.this, newdrawer.class));
                                }
                            }
                            if(status.equals("provider")){
                                if(dataSnapshot.hasChild("current_status")){
                                    if(dataSnapshot.child("current_status").getValue().toString().equals("customer")){
                                        startActivity(new Intent(newlogin.this, newdrawer.class));
                                    }
                                    if(dataSnapshot.child("current_status").getValue().toString().equals("provider")){
                                        startActivity(new Intent(newlogin.this, provider_home.class));
                                    }
                                }
                                else {
                                    startActivity(new Intent(newlogin.this, provider_home.class));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }


            }else {
                Toast.makeText(newlogin.this,"Login Failed",Toast.LENGTH_SHORT).show();
            }

            // ...
        }
    });
}

    public void loginuser() {
        pd.setMessage("Logging in...");
        pd.show();
        String email, pass;
        email = etmail.getText().toString().trim();
        pass = etpass.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            pd.dismiss();
            Toast.makeText(this, "Please enter the username and password!", Toast.LENGTH_SHORT).show();
        } else {
            fauth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(newlogin.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                dbr.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        status = dataSnapshot.child(fauth.getCurrentUser().getUid()).child("status").getValue().toString();
                                        if(status.equals("customer")) {
                                            if(dataSnapshot.hasChild("current_status")){
                                                if(dataSnapshot.child("current_status").getValue().toString().equals("customer")){
                                                    startActivity(new Intent(newlogin.this, newdrawer.class));
                                                }
                                                if(dataSnapshot.child("current_status").getValue().toString().equals("provider")){
                                                    startActivity(new Intent(newlogin.this, provider_home.class));
                                                }
                                            }
                                            else {
                                                startActivity(new Intent(newlogin.this, newdrawer.class));
                                            }
                                        }
                                        if(status.equals("provider")){
                                            if(dataSnapshot.hasChild("current_status")){
                                                if(dataSnapshot.child("current_status").getValue().toString().equals("customer")){
                                                    startActivity(new Intent(newlogin.this, newdrawer.class));
                                                }
                                                if(dataSnapshot.child("current_status").getValue().toString().equals("provider")){
                                                    startActivity(new Intent(newlogin.this, provider_home.class));
                                                }
                                            }
                                            else {
                                                startActivity(new Intent(newlogin.this, provider_home.class));
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            } else {
                                pd.dismiss();
                                Toast.makeText(newlogin.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public  void reset_password(){
        Intent intent=new Intent(newlogin.this,password_reset.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
}