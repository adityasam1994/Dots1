package com.example.aditya.dots1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
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
import com.hbb20.CountryCodePicker;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    public static final int CAPTURE_IMAGE = 29364;
    public static final int PIC_CROP = 74583;
    private static final int CAPTURE_IMAGE_FROM_CAM = 19373;
    private static final int CROP_PIC = 33445;
    Button saveaccount, changeaddress, changeaddress_work;
    EditText etfname, etlname, etaddress, etphone, etaddress_work;
    ImageView profilepic, getloc, btnback, getloc_work;
    Uri profile, filePath, myuri, testuri;
    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Users");
    FirebaseAuth fauth = FirebaseAuth.getInstance();
    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    String piclink;
    LinearLayout workaddress_layout;
    Boolean pickfromlink = false, addressfound = false, addressfound_work = false;
    ProgressDialog pd;
    double lat = 0, lng = 0;
    Boolean showaddress = false, showaddress_work = false, number_valid = false;
    LocationManager locationManager;
    CountryCodePicker ccp;
    String current_status;
    Uri captureuri, inputuri;
    GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myaccount);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        ccp = (CountryCodePicker) findViewById(R.id.code_picker);
        workaddress_layout = (LinearLayout) findViewById(R.id.linearLayout_work);
        changeaddress = (Button) findViewById(R.id.btnchangeadd);
        btnback = (ImageView) findViewById(R.id.btnback);
        getloc = (ImageView) findViewById(R.id.getloc);
        profilepic = (ImageView) findViewById(R.id.profilepic);
        saveaccount = (Button) findViewById(R.id.saveaccount);
        etfname = (EditText) findViewById(R.id.etfname);
        etlname = (EditText) findViewById(R.id.etlname);
        etaddress = (EditText) findViewById(R.id.etaddress);
        etphone = (EditText) findViewById(R.id.etphone);
        etaddress_work = (EditText) findViewById(R.id.etaddress_work);
        changeaddress_work = (Button) findViewById(R.id.btnchangeadd_work);
        getloc_work = (ImageView) findViewById(R.id.getloc_work);

        pd = new ProgressDialog(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            }
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        ccp.registerCarrierNumberEditText(etphone);

        ccp.setPhoneNumberValidityChangeListener(new CountryCodePicker.PhoneNumberValidityChangeListener() {
            @Override
            public void onValidityChanged(boolean isValidNumber) {
                if (etphone.getText().toString().length() > 1) {
                    if (isValidNumber) {
                        number_valid = true;
                    } else {
                        number_valid = false;
                    }
                }
            }
        });

        dbr.child(fauth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String address, phone;

                String fname = dataSnapshot.child("fname").getValue().toString();
                String lname = dataSnapshot.child("lname").getValue().toString();

                if (dataSnapshot.hasChild("address")) {
                    address = dataSnapshot.child("address").getValue().toString();
                } else {
                    address = "";
                }

                if (dataSnapshot.hasChild("ph")) {
                    phone = dataSnapshot.child("ph").getValue().toString();
                } else {
                    phone = "";
                }

                if (dataSnapshot.hasChild("profilepic")) {
                    piclink = dataSnapshot.child("profilepic").getValue().toString();

                    //Toast.makeText(myaccount.this, piclink, Toast.LENGTH_SHORT).show();
                    Picasso.get().load(piclink).resize(200, 200).into(profilepic);
                }
                if (dataSnapshot.hasChild("current_status")) {
                    if (dataSnapshot.child("current_status").getValue().toString().equals("provider")) {
                        current_status = "provider";
                        if (dataSnapshot.hasChild("info")) {
                            String work_address = dataSnapshot.child("info").child("eaddress").getValue().toString();
                            etaddress_work.setText(work_address);
                        }
                    } else {
                        current_status = "customer";
                        LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                        workaddress_layout.setLayoutParams(para);
                    }
                } else {
                    if (dataSnapshot.child("status").getValue().toString().equals("provider")) {
                        String work_address = dataSnapshot.child("info").child("eaddress").getValue().toString();
                        etaddress_work.setText(work_address);
                    } else {
                        current_status = "customer";
                        LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                        workaddress_layout.setLayoutParams(para);
                    }
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

        storageReference.child("images/" + (fauth.getCurrentUser().getUid())).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        profile = uri;

                        if (profile != null) {
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

        getloc_work.setOnClickListener(new View.OnClickListener() {
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
                } else {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            Criteria criteria = new Criteria();
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);
                            String provider = locationManager.getBestProvider(criteria, true);
                            locationManager.requestLocationUpdates(provider, 0, 0, myaccount.this);


                            showaddress_work = true;
                            etaddress_work.setEnabled(false);
                            changeaddress_work.setVisibility(View.VISIBLE);
                            pd.setMessage("Fetching Location...");
                            pd.show();
                            getloc();

                        } else {
                            String[] requestloc = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                            requestPermissions(requestloc, REQUEST_LOC);
                        }
                    } else {
                        Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        String provider = locationManager.getBestProvider(criteria, true);
                        locationManager.requestLocationUpdates(provider, 0, 0, myaccount.this);


                        showaddress_work = true;
                        etaddress_work.setEnabled(false);
                        changeaddress_work.setVisibility(View.VISIBLE);
                        pd.setMessage("Fetching Location...");
                        pd.show();
                        getloc();
                    }
                }
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
                } else {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            Criteria criteria = new Criteria();
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);
                            String provider = locationManager.getBestProvider(criteria, true);
                            locationManager.requestLocationUpdates(provider, 0, 0, myaccount.this);


                            showaddress = true;
                            etaddress.setEnabled(false);
                            changeaddress.setVisibility(View.VISIBLE);
                            pd.setMessage("Fetching Location...");
                            pd.show();
                            getloc();

                        } else {
                            String[] requestloc = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                            requestPermissions(requestloc, REQUEST_LOC);
                        }
                    } else {
                        Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        String provider = locationManager.getBestProvider(criteria, true);
                        locationManager.requestLocationUpdates(provider, 0, 0, myaccount.this);


                        showaddress = true;
                        etaddress.setEnabled(false);
                        changeaddress.setVisibility(View.VISIBLE);
                        pd.setMessage("Fetching Location...");
                        pd.show();
                        getloc();
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

        changeaddress_work.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder add_builder = new AlertDialog.Builder(myaccount.this);
                add_builder.setTitle("Change address");
                add_builder.setMessage("Do you want to change the address");
                add_builder.setPositiveButton("Change address", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etaddress_work.setEnabled(true);
                        changeaddress_work.setVisibility(View.INVISIBLE);
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

                double latitude = 0, longitude = 0;
                double latitude_work = 0, longitude_work = 0;

                if (etaddress.isEnabled()) {
                    Geocoder geocoder = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocationName(etaddress.getText().toString(), 1);
                        if (addresses.size() > 0) {
                            latitude = addresses.get(0).getLatitude();
                            longitude = addresses.get(0).getLongitude();
                            addressfound = true;
                        } else {
                            Toast.makeText(myaccount.this, "Address not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (showaddress) {
                        latitude = lat;
                        longitude = lng;
                    }
                }

                if (etaddress_work.isEnabled()) {
                    Geocoder geocoder = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocationName(etaddress_work.getText().toString(), 1);
                        if (addresses.size() > 0) {
                            latitude_work = addresses.get(0).getLatitude();
                            longitude_work = addresses.get(0).getLongitude();
                            addressfound_work = true;
                        } else {
                            Toast.makeText(myaccount.this, "Address not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (showaddress_work) {
                        latitude_work = lat;
                        longitude_work = lng;
                    }
                }

                String fname = "", lname = "", address = "", phone = "", address_work = "";
                fname = etfname.getText().toString().trim();
                lname = etlname.getText().toString().trim();
                address = etaddress.getText().toString().trim();
                phone = etphone.getText().toString().trim();

                dbr.child(fauth.getCurrentUser().getUid()).child("fname").setValue(fname);
                dbr.child(fauth.getCurrentUser().getUid()).child("lname").setValue(lname);
                dbr.child(fauth.getCurrentUser().getUid()).child("address").setValue(address);
                dbr.child(fauth.getCurrentUser().getUid()).child("lati").setValue(latitude);
                dbr.child(fauth.getCurrentUser().getUid()).child("longi").setValue(longitude);

                if (current_status.equals("provider")) {
                    address_work = etaddress_work.getText().toString().trim();
                    dbr.child(fauth.getCurrentUser().getUid()).child("info").child("eaddress").setValue(address_work);
                    dbr.child(fauth.getCurrentUser().getUid()).child("info").child("lati").setValue(latitude_work);
                    dbr.child(fauth.getCurrentUser().getUid()).child("info").child("longi").setValue(longitude_work);
                }

                if (number_valid) {
                    dbr.child(fauth.getCurrentUser().getUid()).child("ph").setValue(phone);
                }

                StorageReference strf = storageReference.child("images/" + fauth.getCurrentUser().getUid());


                if (filePath == null) {
                    if (profile != null) {
                        Toast.makeText(myaccount.this, "Account update successful", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    } else {
                        //dbr.child(fauth.getCurrentUser().getUid()).child("profilepic").setValue(piclink);
                        pd.dismiss();
                        Toast.makeText(myaccount.this, "Account updated successfully", Toast.LENGTH_SHORT).show();
                    }
                } else {
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
        });

        profilepic.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                               /* Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.putExtra("crop", "true");
                                intent.putExtra("aspectX", 1);
                                intent.putExtra("aspectY",1);
                                intent.putExtra("outputX", 500);
                                intent.putExtra("outputY",500);
                                intent.putExtra("return-data",true);
                                startActivityForResult(intent, CAPTURE_IMAGE);*/
                        camorgallery();


                    } else {
                        String[] reqStorage = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(reqStorage, REQ_STORAGE_MY);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        CropImage.activity()
                                .setAspectRatio(1, 1)
                                .setRequestedSize(500, 500)
                                .setCropShape(CropImageView.CropShape.OVAL)
                                .start(myaccount.this);
                    } else {
                        String[] reqStorage = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(reqStorage, REQ_STORAGE_MY);
                    }
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    CropImage.activity()
                            .setAspectRatio(1, 1)
                            .setRequestedSize(500, 500)
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .start(myaccount.this);
                }

            }
        });
    }


    private void camorgallery() {
        final Dialog dialog = new Dialog(myaccount.this);
        dialog.setContentView(R.layout.camorgallery);
        dialog.show();
        Button cam = dialog.findViewById(R.id.cam);
        Button gal = dialog.findViewById(R.id.gal);

        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String storagedir = Environment.getExternalStorageDirectory().getAbsolutePath();
                File dir = new File(storagedir, "/Dot/");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String picname = "profile.jpg";

                File file = new File(dir, picname);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                inputuri = FileProvider.getUriForFile(myaccount.this, BuildConfig.APPLICATION_ID + ".provider", file);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, inputuri);
                startActivityForResult(intent, CAPTURE_IMAGE_FROM_CAM);
                dialog.dismiss();
            }
        });

        gal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 500);
                intent.putExtra("outputY", 500);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CAPTURE_IMAGE);*/
                Crop.pickImage(myaccount.this);
                dialog.dismiss();
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
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
        if (requestCode == REQUEST_LOC) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) this);

                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQ_STORAGE_MY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Read permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == 1000){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(myaccount.this, "Failed" + error, Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE) {
                filePath = data.getData();
            }
        }

        if (requestCode == CAPTURE_IMAGE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap image = extras.getParcelable("data");
                //profilepic.setImageBitmap(image);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), image, "profile", null);
                Uri newuri = Uri.parse(path);
                profilepic.setImageURI(newuri);
                filePath = newuri;
            }
        }

        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }

        if (requestCode == CAPTURE_IMAGE_FROM_CAM && resultCode == RESULT_OK) {
            beginCrop(inputuri);
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            profilepic.setImageURI(Crop.getOutput(result));
            filePath = Crop.getOutput(result);
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performCrop() {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(captureuri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 500);
            cropIntent.putExtra("outputY", 500);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, CROP_PIC);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "This device doesn't support cropping!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        /*lat = location.getLatitude();
        lng = location.getLongitude();
        Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.isEmpty()) {
                etaddress.setText("Waiting for address");
            } else {
                if (addresses.size() > 0 && showaddress == true) {
                    etaddress.setText("");

                    String ad = addresses.get(0).getAddressLine(0);
                    etaddress.setText(ad);
                    pd.dismiss();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            List<Address> addresses_work = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses_work.isEmpty()) {
                etaddress_work.setText("Waiting for address");
            } else {
                if (addresses_work.size() > 0 && showaddress_work == true) {
                    etaddress_work.setText("");

                    String ad = addresses_work.get(0).getAddressLine(0);
                    etaddress_work.setText(ad);
                    pd.dismiss();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
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

    @SuppressLint("MissingPermission")
    private void getloc() {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    Geocoder geo = new Geocoder(myaccount.this.getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses.isEmpty()) {
                            etaddress.setText("Waiting for address");
                        } else {
                            if (addresses.size() > 0 && showaddress == true) {
                                etaddress.setText("");

                                String ad = addresses.get(0).getAddressLine(0);
                                etaddress.setText(ad);
                                pd.dismiss();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        List<Address> addresses_work = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses_work.isEmpty()) {
                            etaddress_work.setText("Waiting for address");
                        } else {
                            if (addresses_work.size() > 0 && showaddress_work == true) {
                                etaddress_work.setText("");

                                String ad = addresses_work.get(0).getAddressLine(0);
                                etaddress_work.setText(ad);
                                pd.dismiss();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    getloc();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(myaccount.this, "Failed to get the location. please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}