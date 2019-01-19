package com.example.aditya.dots1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class newsignup extends AppCompatActivity implements LocationListener, View.OnClickListener {

    public static final int CAMERA_PERMISSION_REQUEST = 12345678;
    public static final int REQUSET_FINE_LOCATION = 9999;
    public static final int REQUEST_COARSE_LOCATION = 8888;
    public static final int REQ_LOC_SIGN = 907823;
    String[] namewords;
    LoginButton flogin;
    CallbackManager callbackManager;
    GoogleApiClient googleApiClient;
    AccessToken accessToken;
    Button btnreg, btnloc, btnchangeaddress;
    ImageButton btnimage, btnfblogin;
    ImageView ivprofile;
    int PICK_IMAGE_REQUEST = 111, TAKE_PICTURE = 0;
    Uri filePath;
    EditText etfname, etlname, etmail, etpass, etpass2, etphone, etaddress;
    FirebaseAuth fauth;
    DatabaseReference dbr;
    ProgressDialog pd;
    TextView txtclose, camera, gallery, li;
    Dialog mydialogue;
    int RC_SIGN_IN = 101;
    ImageButton gsign;
    FirebaseAuth.AuthStateListener authStateListener;
    GoogleSignInClient mGoogleSignInClient;
    public String name, lname, fname = "";
    private RequestQueue requestQueue;
    private LocationManager locationManager;
    public boolean showaddress = false, addressfound = false;
    double latitude, longitude;

    //creating reference to firebase storage
    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsignup);

        pd = new ProgressDialog(this);
        li = (TextView) findViewById(R.id.btnstart);
        ivprofile = (ImageView) findViewById(R.id.profilepic);
        btnreg = (Button) findViewById(R.id.btnreg);
        etfname = (EditText) findViewById(R.id.etfname);
        etlname = (EditText) findViewById(R.id.etlname);
        etmail = (EditText) findViewById(R.id.etmail);
        etpass = (EditText) findViewById(R.id.etpass);
        etpass2 = (EditText) findViewById(R.id.etpass2);
        etphone = (EditText) findViewById(R.id.etphone);
        etaddress = (EditText) findViewById(R.id.etaddress);
        mydialogue = new Dialog(this);
        gsign = (ImageButton) findViewById(R.id.gsign);
        btnloc = (Button) findViewById(R.id.btnloc);
        btnchangeaddress = (Button) findViewById(R.id.btnchangeaddress);
        ivprofile.setClickable(true);

        btnfblogin = (ImageButton) findViewById(R.id.fsigninbutton);
        flogin = (LoginButton) findViewById(R.id.fsignin);

        requestQueue = Volley.newRequestQueue(this);
        callbackManager = CallbackManager.Factory.create();
        flogin.setReadPermissions("email", "public_profile");
        flogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Toast.makeText(newsignup.this, "Success", Toast.LENGTH_SHORT).show();
                pd.setMessage("Logging in...");
                pd.show();
                handleaccesstoken(loginResult.getAccessToken(), name);
            }

            @Override
            public void onCancel() {
                Toast.makeText(newsignup.this, "Canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(newsignup.this, "Oop! Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(newsignup.this, user.getDisplayName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(newsignup.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        };
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //LoginManager.getInstance().logOut();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //requestlocation();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) this);
            }
            else {
                requestlocation();
            }
        }*/





        //final ConstraintLayout constraintLayout=(ConstraintLayout)findViewById(R.id.signupcontraint);
        final float density=getResources().getDisplayMetrics().density;
        final Drawable fname=getResources().getDrawable(R.drawable.u30);
        final Drawable lname=getResources().getDrawable(R.drawable.u30);
        final Drawable mail=getResources().getDrawable(R.drawable.emailicon);
        final Drawable lock=getResources().getDrawable(R.drawable.locki);
        final Drawable phone=getResources().getDrawable(R.drawable.phoneicon);
        final Drawable home=getResources().getDrawable(R.drawable.hom);

        final  int width=Math.round(24*density);
        final int height=Math.round(24*density);

        fname.setBounds(0,0,width,height);
        etfname.setCompoundDrawables(fname,null,null,null);

        lname.setBounds(0,0,width,height);
        etlname.setCompoundDrawables(lname,null,null,null);

        mail.setBounds(0,0,width,height);
        etmail.setCompoundDrawables(mail,null,null,null);

        lock.setBounds(0,0,width,height);
        etpass.setCompoundDrawables(lock,null,null,null);
        etpass2.setCompoundDrawables(lock,null,null,null);

        phone.setBounds(0,0,width,height);
        etphone.setCompoundDrawables(phone,null,null,null);

        home.setBounds(0,0,width,height);
        etaddress.setCompoundDrawables(home,null,null,null);

        fauth=FirebaseAuth.getInstance();
        dbr= FirebaseDatabase.getInstance().getReference("Users");

        btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registeruser();
            }
        });

        li.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openlogin();
            }
        });

        gsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   /* Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                    googleApiClient.connect();*/

                pd.setMessage("Logging in...");
                pd.show();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        ivprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            takepicture();

                    }
                    else {
                        String[] pemissionRequest={Manifest.permission.CAMERA};
                        requestPermissions(pemissionRequest, CAMERA_PERMISSION_REQUEST);
                    }
                }
            }
        });

        btnloc.setOnClickListener(this);

        btnchangeaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnchangeaddress.setVisibility(View.INVISIBLE);
                etaddress.setFocusable(true);AlertDialog.Builder add_builder = new AlertDialog.Builder(newsignup.this);
                add_builder.setTitle("Change address");
                add_builder.setMessage("Do you want to change the address");
                add_builder.setPositiveButton("Change address", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etaddress.setEnabled(true);
                        btnchangeaddress.setVisibility(View.INVISIBLE);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(newsignup.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                });
                add_builder.show();

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == CAMERA_PERMISSION_REQUEST){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                takepicture();
            }
        }

        if(requestCode == REQ_LOC_SIGN){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Location Permission granted", Toast.LENGTH_SHORT).show();
            }

            else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void takepicture() {
        CropImage.activity()
                .setAspectRatio(1,1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setRequestedSize(500,500)
                .start(newsignup.this);
    }

    public void onClicfb(View view){
        if(view == btnfblogin){
            flogin.performClick();
        }
    }

    private void requestlocation() {
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_COARSE_LOCATION},980);
    }



    /*@Override
    protected void onStart() {
        super.onStart();
        googleApiClient.disconnect();
        //if the user is already signed in
        //we will close this activity
        //and take the user to profile activity
        if (fauth.getCurrentUser() != null) {
            *//*finish();
            startActivity(new Intent(this, home.class));*//*
            Toast.makeText(this, "Already Logged in", Toast.LENGTH_SHORT).show();

        }
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                ivprofile.setImageURI(filePath);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(newsignup.this,"Failed"+error,Toast.LENGTH_SHORT).show();
            }
        }

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

    private void handleaccesstoken(final AccessToken token, final String name){
        AuthCredential credential=FacebookAuthProvider.getCredential(token.getToken());
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

                            try {
                                InputStream stream=new FileInputStream(new File(photo));
                                StorageReference s=storageRef.child("myimage");
                                s.putStream(stream);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            pd.dismiss();

                            Intent intent = new Intent(newsignup.this, select.class);
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

                                        if(st.equals("customer")) {
                                            if(dataSnapshot.child(fauth.getCurrentUser().getUid()).hasChild("current_status")){
                                                if(dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("customer")){
                                                    pd.dismiss();
                                                    startActivity(new Intent(newsignup.this, newdrawer.class));
                                                }
                                                if(dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("provider")){
                                                    pd.dismiss();
                                                    startActivity(new Intent(newsignup.this, provider_home.class));
                                                }
                                            }
                                            else {
                                                pd.dismiss();
                                                startActivity(new Intent(newsignup.this, newdrawer.class));
                                            }}

                                        if(st.equals("provider")){
                                            if(dataSnapshot.child(fauth.getCurrentUser().getUid()).hasChild("current_status")){
                                                if(dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("customer")){
                                                    pd.dismiss();
                                                    startActivity(new Intent(newsignup.this, newdrawer.class));
                                                }
                                                if(dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("provider")){
                                                    pd.dismiss();
                                                    startActivity(new Intent(newsignup.this, provider_home.class));
                                                }
                                            }

                                            pd.dismiss();
                                            startActivity(new Intent(newsignup.this, provider_home.class));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                        }else {
                            Toast.makeText(newsignup.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        fauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Boolean isnewuser = task.getResult().getAdditionalUserInfo().isNewUser();


                            if (isnewuser) {

                                FirebaseUser user = fauth.getCurrentUser();
                                String name = account.getDisplayName();
                                Uri photouri = (account.getPhotoUrl());
                                String photstring = photouri.toString();

                                String[] splitname = getnamewords(name);
                                if (splitname.length > 1) {
                                    lname = splitname[splitname.length - 1];
                                    fname = name.replace(lname, "").trim();
                                }

                                gsignin gsign = new gsignin(fname, lname);
                                dbr.child(user.getUid()).setValue(gsign);
                                dbr.child(user.getUid()).child("profilepic").setValue(photstring);

                                pd.dismiss();

                                Intent intent = new Intent(newsignup.this, select.class);
                                startActivity(intent);

                            }
                            else {
                                dbr.child(fauth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String st=dataSnapshot.child("status").getValue().toString();

                                        if(st.equals("customer")) {
                                            if(dataSnapshot.child(fauth.getCurrentUser().getUid()).hasChild("current_status")){
                                                if(dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("customer")){
                                                    pd.dismiss();
                                                    startActivity(new Intent(newsignup.this, newdrawer.class));
                                                }
                                                if(dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("provider")){
                                                    pd.dismiss();
                                                    startActivity(new Intent(newsignup.this, provider_home.class));
                                                }
                                            }
                                            else {
                                                pd.dismiss();
                                                startActivity(new Intent(newsignup.this, newdrawer.class));
                                            }}

                                        if(st.equals("provider")){
                                            if(dataSnapshot.child(fauth.getCurrentUser().getUid()).hasChild("current_status")){
                                                if(dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("customer")){
                                                    pd.dismiss();
                                                    startActivity(new Intent(newsignup.this, newdrawer.class));
                                                }
                                                if(dataSnapshot.child(fauth.getCurrentUser().getUid()).child("current_status").getValue().toString().equals("provider")){
                                                    pd.dismiss();
                                                    startActivity(new Intent(newsignup.this, provider_home.class));
                                                }
                                            }

                                            pd.dismiss();
                                            startActivity(new Intent(newsignup.this, provider_home.class));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                        }
                        else {
                            Toast.makeText(newsignup.this,"Login Failed",Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private String[] getnamewords(String name) {
        if(name != null){
            namewords=name.split("\\s+");

        }
        return namewords;
    }


    public void openlogin(){
        Intent intent=new Intent(this,newlogin.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private  void requeststorage(){
        ActivityCompat.requestPermissions(this,new String[]{READ_EXTERNAL_STORAGE},1);
    }



    public  void registeruser(){
        pd.setMessage("Registering....");
        pd.show();
        final String email,pass;
        double lat=0.0;
        double lng=0.0;
        String fname, lname, ph, address, pass2;
        fname = etfname.getText().toString().trim();
        lname = etlname.getText().toString().trim();
        ph = etphone.getText().toString().trim();
        address = etaddress.getText().toString().trim();
        pass2 = etpass2.getText().toString().trim();
        email=etmail.getText().toString().trim();
        pass=etpass.getText().toString().trim();

        if(fname.isEmpty() || lname.isEmpty() || ph.isEmpty() || address.isEmpty() || email.isEmpty() || pass.isEmpty() || filePath == null) {
            pd.dismiss();
            Toast.makeText(newsignup.this,"All fields are required!",Toast.LENGTH_SHORT).show();
        }
        else {
            if(!pass.equals(pass2)){
                pd.dismiss();
                Toast.makeText(newsignup.this,"Please enter same password in both password fields",Toast.LENGTH_SHORT).show();
            }
            else {
                fauth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String fnam, lname, ph, address;
                                    double lati = 0, longi = 0;

                                    if (etaddress.isFocusable()) {
                                        Geocoder geocoder = new Geocoder(newsignup.this.getApplicationContext(), Locale.getDefault());
                                        List<Address> addresses;
                                        try {
                                            addresses = geocoder.getFromLocationName(etaddress.getText().toString(), 1);
                                            if (addresses.size() > 0) {
                                                lati = addresses.get(0).getLatitude();
                                                longi = addresses.get(0).getLongitude();
                                                addressfound = true;
                                            } else {
                                                Toast.makeText(newsignup.this, "Address not found", Toast.LENGTH_SHORT).show();
                                                addressfound = false;
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        lati = latitude;
                                        longi = longitude;
                                    }
                                    if (addressfound || !etaddress.isEnabled()) {
                                        String fnames = etfname.getText().toString().trim();
                                        lname = etlname.getText().toString().trim();
                                        ph = etphone.getText().toString().trim();
                                        address = etaddress.getText().toString().trim();
                                        usersignup usignup = new usersignup(fnames, lname, ph, address, lati, longi);

                                        dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(usignup);
                                        StorageReference childRef = storageRef.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        //uploading the image
                                        if (filePath != null) {
                                            childRef.putFile(filePath);
                                        } else {
                                            Uri uri = Uri.parse("android:resource://com.example.aditya.dots1/drawable/cam");
                                            childRef.putFile(uri);
                                        }

                                        //*fauth.signInWithEmailAndPassword(email,pass);*//*
                                        pd.dismiss();
                                        Intent intent = new Intent(newsignup.this, select.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                                    }
                                }else {
                                    Toast.makeText(newsignup.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                }
                            }
                        });
            }

        }


    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    @Override
    public void onLocationChanged(final Location location) {
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        Geocoder geo = new Geocoder(newsignup.this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.isEmpty()) {
                etaddress.setText("Waiting for address");
                        } else {
                            if (addresses.size() > 0 && showaddress==true && etaddress.getText().toString().isEmpty()) {
                                String fn = addresses.get(0).getFeatureName();
                                String loc = addresses.get(0).getLocality();
                                String aa = addresses.get(0).getAdminArea();
                                String cn = addresses.get(0).getCountryName();
                                String pc = addresses.get(0).getPostalCode();

                                if (fn == null) {
                                    fn = "";
                                }
                                if (loc == null) {
                                    loc = "";
                                }
                                if (aa == null) {
                                    aa = "";
                                }
                                if (cn == null) {
                                    cn = "";
                                }
                                if (pc == null) {
                                    pc = "";
                                }

                                String ad = fn + "," + loc + "," + aa + "," + cn + "," + pc;
                                etaddress.setText(ad);
                                pd.dismiss();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "GPS service is disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnloc:
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(newsignup.this);
                    builder.setTitle("GPS Disabled!");
                    builder.setMessage("GPS should be enabled to get your location");
                    builder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(newsignup.this, "Okay", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();
                }
                else{

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            Criteria criteria = new Criteria();
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);
                            String provider = locationManager.getBestProvider(criteria, true);
                            locationManager.requestLocationUpdates(provider, 0, 0,  this);

                            etaddress.setText("");
                            showaddress=true;
                            etaddress.setEnabled(false);
                            btnchangeaddress.setVisibility(View.VISIBLE);
                            pd.setMessage("Fetching Location...");
                            pd.show();
                        }
                        else {
                            String[] req_loc=new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                            requestPermissions(req_loc, REQ_LOC_SIGN);
                        }
                    }
                    else {
                        Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        String provider = locationManager.getBestProvider(criteria, true);
                        locationManager.requestLocationUpdates(provider, 0, 0,  this);

                        etaddress.setText("");
                        showaddress=true;
                        etaddress.setEnabled(false);
                        btnchangeaddress.setVisibility(View.VISIBLE);
                        pd.setMessage("Fetching Location...");
                        pd.show();
                    }

                }
                break;
        }
    }
}
