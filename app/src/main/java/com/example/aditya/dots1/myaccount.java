package com.example.aditya.dots1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class myaccount extends AppCompatActivity implements LocationListener {

    public static final int REQUEST_LOC = 67564;
    private static final int REQ_STORAGE_WRITE_MY = 796786;
    private static final int REQ_STORAGE_MY = 98656;
    Button saveaccount, changeaddress;
    EditText etfname, etlname, etaddress, etphone;
    ImageView profilepic, getloc, btnback;
    Uri profile, filePath;
    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Users");
    FirebaseAuth fauth = FirebaseAuth.getInstance();
    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    String piclink;
    Boolean pickfromlink = false, addressfound=false;
    ProgressDialog pd;
    double lat = 0, lng = 0;
    Boolean showaddress = false;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myaccount);

        changeaddress=(Button)findViewById(R.id.btnchangeadd);
        btnback=(ImageView)findViewById(R.id.btnback);
        getloc = (ImageView) findViewById(R.id.getloc);
        profilepic = (ImageView) findViewById(R.id.profilepic);
        saveaccount = (Button) findViewById(R.id.saveaccount);
        etfname = (EditText) findViewById(R.id.etfname);
        etlname = (EditText) findViewById(R.id.etlname);
        etaddress = (EditText) findViewById(R.id.etaddress);
        etphone = (EditText) findViewById(R.id.etphone);

        pd = new ProgressDialog(this);

        final float density=getResources().getDisplayMetrics().density;

        final Drawable fname=getResources().getDrawable(R.drawable.u30);
        final Drawable phone=getResources().getDrawable(R.drawable.iconfinder_phone);
        final Drawable home=getResources().getDrawable(R.drawable.hom);

        final  int width=Math.round(24*density);
        final int height=Math.round(24*density);

        fname.setBounds(0,0,width,height);
        etfname.setCompoundDrawables(fname,null,null,null);
        etlname.setCompoundDrawables(fname,null,null,null);

        home.setBounds(0,0,width,height);
        etaddress.setCompoundDrawables(home,null,null,null);

        phone.setBounds(0,0,width,height);
        etphone.setCompoundDrawables(phone,null,null,null);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        dbr.child(fauth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String address, phone;

                String fname=dataSnapshot.child("fname").getValue().toString();
                String lname=dataSnapshot.child("lname").getValue().toString();

                if(dataSnapshot.hasChild("address")) {
                    address = dataSnapshot.child("address").getValue().toString();
                }
                else {
                    address="";
                }

                if(dataSnapshot.hasChild("ph")) {
                    phone = dataSnapshot.child("ph").getValue().toString();
                }
                else {
                    phone="";
                }

                if(dataSnapshot.hasChild("profilepic")){
                    piclink=dataSnapshot.child("profilepic").getValue().toString();

                    //Toast.makeText(myaccount.this, piclink, Toast.LENGTH_SHORT).show();
                    Picasso.get().load(piclink).resize(200, 200).into(profilepic);
                }

                etfname.setText(fname);
                etlname.setText(lname);
                etaddress.setText(address);
                etphone.setText(phone);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        storageReference.child("images/"+(fauth.getCurrentUser().getUid())).getDownloadUrl()
        .addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                profile=uri;

                if(profile != null){
                    Picasso.get().load(profile).resize(200, 200).into(profilepic);
                }

            }
        });


        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(myaccount.this);
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
                            Toast.makeText(myaccount.this, "Okay", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();
                }
                else{

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            Criteria criteria = new Criteria();
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);
                            String provider = locationManager.getBestProvider(criteria, true);
                            locationManager.requestLocationUpdates(provider, 0, 0, myaccount.this);


                            showaddress=true;
                            etaddress.setEnabled(false);
                            changeaddress.setVisibility(View.VISIBLE);
                            pd.setMessage("Fetching Location...");
                            pd.show();

                        }
                        else {
                            String[] requestloc = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                            requestPermissions(requestloc, REQUEST_LOC);
                        }
                    }
                    else {
                        Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        String provider = locationManager.getBestProvider(criteria, true);
                        locationManager.requestLocationUpdates(provider, 0, 0, myaccount.this);


                        showaddress=true;
                        etaddress.setEnabled(false);
                        changeaddress.setVisibility(View.VISIBLE);
                        pd.setMessage("Fetching Location...");
                        pd.show();
                    }
                }
            }
        });

        changeaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder add_builder = new AlertDialog.Builder(myaccount.this);
                add_builder.setTitle("Change address");
                add_builder.setMessage("Do you want to change the address");
                add_builder.setPositiveButton("Change address", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etaddress.setEnabled(true);
                        changeaddress.setVisibility(View.INVISIBLE);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(myaccount.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                });
                add_builder.show();
            }
        });

        saveaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Updating Account...");
                pd.show();

                double latitude=0,longitude=0;

                if(etaddress.isEnabled() && etaddress.getText().toString().length() >= 5) {
                    Geocoder geocoder=new Geocoder(myaccount.this.getApplicationContext(),Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses=geocoder.getFromLocationName(etaddress.getText().toString(),1);
                        if(addresses.size()>0){
                            latitude=addresses.get(0).getLatitude();
                            longitude=addresses.get(0).getLongitude();
                            addressfound=true;
                        }
                        else {
                            Toast.makeText(myaccount.this, "Address not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    if(showaddress) {
                        latitude = lat;
                        longitude = lng;
                    }
                }

                if(addressfound == true || !etaddress.isEnabled() || etaddress.getText().toString().length() < 5) {

                    String fname="", lname="", address="", phone="";
                    fname = etfname.getText().toString().trim();
                    lname = etlname.getText().toString().trim();
                    address = etaddress.getText().toString().trim();
                    phone = etphone.getText().toString().trim();

                    dbr.child(fauth.getCurrentUser().getUid()).child("fname").setValue(fname);
                    dbr.child(fauth.getCurrentUser().getUid()).child("lname").setValue(lname);
                    dbr.child(fauth.getCurrentUser().getUid()).child("address").setValue(address);
                    dbr.child(fauth.getCurrentUser().getUid()).child("ph").setValue(phone);
                    dbr.child(fauth.getCurrentUser().getUid()).child("lati").setValue(latitude);
                    dbr.child(fauth.getCurrentUser().getUid()).child("longi").setValue(longitude);

                    StorageReference strf = storageReference.child("images/" + fauth.getCurrentUser().getUid());


                    if(filePath == null){
                        if(profile != null){
                            Toast.makeText(myaccount.this, "Account update successful", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                        else {
                            dbr.child(fauth.getCurrentUser().getUid()).child("profilepic").setValue(piclink);
                            pd.dismiss();
                            Toast.makeText(myaccount.this, "Account updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        strf.putFile(filePath)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Toast.makeText(myaccount.this, "Account update successful", Toast.LENGTH_SHORT).show();
                                        dbr.child(fauth.getCurrentUser().getUid()).child("profilepic").removeValue();
                                        pd.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(myaccount.this, "Failed to update account!", Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    }
                                });
                    }

                }
            }
        });

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        CropImage.activity()
                                .setAspectRatio(1, 1)
                                .setRequestedSize(500, 500)
                                .setCropShape(CropImageView.CropShape.OVAL)
                                .start(myaccount.this);
                    }
                    else {
                        String[] reqStorage=new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(reqStorage, REQ_STORAGE_MY);
                    }
                }
            }
        });
    }



    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_LOC){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) this);
            }
            else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == REQ_STORAGE_MY){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Storage Read permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                profilepic.setImageURI(filePath);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(myaccount.this,"Failed"+error,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lat=location.getLatitude();
        lng=location.getLongitude();
        Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.isEmpty()) {
                etaddress.setText("Waiting for address");
            } else {
                if (addresses.size() > 0 && showaddress==true) {
                    etaddress.setText("");

                    String ad = addresses.get(0).getFeatureName() + ","
                            + addresses.get(0).getLocality() + ","
                            + addresses.get(0).getAdminArea() + ","
                            + addresses.get(0).getCountryName() + ","
                            + addresses.get(0).getPostalCode();
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

    }
}
