package com.example.aditya.dots1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import id.zelory.compressor.Compressor;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.aditya.dots1.R.*;
import static com.example.aditya.dots1.myaccount.REQUEST_LOC;
import static com.example.aditya.dots1.newsignup.CAMERA_PERMISSION_REQUEST;

public class neworder extends AppCompatActivity implements LocationListener, View.OnClickListener {

    public static final int WRITE_STORAGE_PERMISSION = 9876;
    public static final int READ_STORAGE_PERMISSION = 9890;
    public static final int REQUEST_LOC_ORDER = 934758;
    public static final int REQ_READ_ORDER = 856868;
    public static final int REQ_STORAGE_ORD = 45852;
    public static final int REQ_STORAGE_WRITE_ORD = 53942;
    VideoView videoView;
    int capture_video=3;
    double lat=0.00,lng=0.00;
    Spinner spinner, spinner2;
    TextView heading, tvgps;
    ImageView btnimage, btnback;
    Uri filePath;
    String result;
    EditText comment, address;
    Spinner spinner_service, spinner_time;
    Button submit, btnlocation,btnvideo, btnchangeaddress;
    ProgressDialog pd;
    private RequestQueue requestQueue;
    private LocationManager locationManager;
    boolean showaddress=false, addressfound=false;
    boolean addressprint=false;
    int Takepic=1234;
    String code,format,codeforqr,servicename;
    private  File output;
    String imagepath;
    Uri fileuri, outputfileuri;
    Button btnplay, btnplus;
    String cam_or_vid;
    FusedLocationProviderClient fusedLocationProviderClient;

    FirebaseAuth fauth = FirebaseAuth.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://dots-195d9.appspot.com");
    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Orders");
    DatabaseReference dbrservice=FirebaseDatabase.getInstance().getReference("services");
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_neworder);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btnplay=(Button)findViewById(R.id.btnplay);
        btnplus=(Button)findViewById(id.btnplus);
        btnchangeaddress=(Button)findViewById(id.btneditaddress);
        btnvideo=(Button)findViewById(id.btnvideo);
        btnback=(ImageView)findViewById(id.btnback);
        videoView=(VideoView)findViewById(id.videoView);
        requestQueue = Volley.newRequestQueue(this);
        btnlocation = (Button) findViewById(id.btnlocation);
        pd = new ProgressDialog(this);
        spinner = (Spinner) findViewById(id.spinner_service);
        spinner2 = (Spinner) findViewById(id.spinner_time);
        heading = (TextView) findViewById(id.tv);
        btnimage = (ImageView) findViewById(id.btnimage);
        spinner_service = (Spinner) findViewById(id.spinner_service);
        spinner_time = (Spinner) findViewById(id.spinner_time);
        comment = (EditText) findViewById(id.etcomment);
        address = (EditText) findViewById(id.etaddress);
        submit = (Button) findViewById(id.btnstart);

        servicename=getIntent().getExtras().getString("service");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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

        String head = getIntent().getExtras().getString("service");
        heading.setText(head);

        dbrservice.child(getIntent().getExtras().getString("service")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> list_myservice = new ArrayList<>();

                    list_myservice.add("Select service type...");

                    for(DataSnapshot dd:dataSnapshot.getChildren()){
                        list_myservice.add(dd.getKey().toString());
                    }

                    list_myservice.add("Other");

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(neworder.this,
                            android.R.layout.simple_spinner_dropdown_item, list_myservice);

                    spinner.setAdapter(adapter);
                    adapter.setDropDownViewResource(layout.activity_spinner_item);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ArrayList<String> list2 = new ArrayList<>();

        list2.add("Select prefered time...");
        list2.add("10am-1pm");
        list2.add("1pm-4pm");
        list2.add("4pm-7pm");
        list2.add("No preference");

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, list2);


        spinner2.setAdapter(adapter1);
        adapter1.setDropDownViewResource(layout.activity_spinner_item);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((TextView) view).setTextColor(Color.parseColor("#ECECEC"));
                } else {
                    ((TextView) view).setTextColor(Color.parseColor("#3b3b3b"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((TextView) view).setTextColor(Color.parseColor("#ECECEC"));
                } else {
                    ((TextView) view).setTextColor(Color.parseColor("#3b3b3b"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnplus.setOnClickListener(this);

        btnplay.setOnClickListener(this);

        btnimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            camvideo();
                    }
                    else {
                        String[] pemissionRequest=new String[]{Manifest.permission.CAMERA};
                        requestPermissions(pemissionRequest, CAMERA_PERMISSION_REQUEST);
                    }
                }
                else {
                    camvideo();
                }
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnlocation.setOnClickListener(this);

        btnchangeaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder add_builder = new AlertDialog.Builder(neworder.this);
                add_builder.setTitle("Change address");
                add_builder.setMessage("Do you want to change the address?");
                add_builder.setPositiveButton("Change address", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        address.setEnabled(true);
                        btnchangeaddress.setVisibility(View.INVISIBLE);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(neworder.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                });
                add_builder.show();
            }
        });


        submit.setOnClickListener(this);
    }

    private void submit(){
        pd.setMessage("Saving details...");
        pd.show();
        double latitude=0.00,longitude=0.00;


        if(address.isEnabled()) {
            Geocoder geocoder=new Geocoder(neworder.this.getApplicationContext(),Locale.getDefault());
            List<Address> addresses;
            try {
                addresses=geocoder.getFromLocationName(address.getText().toString(),1);
                if(addresses.size()>0){
                    latitude=addresses.get(0).getLatitude();
                    longitude=addresses.get(0).getLongitude();
                    addressfound = true;
                }
                else {
                    //Toast.makeText(neworder.this, "Address not found", Toast.LENGTH_SHORT).show();
                    addressfound = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            latitude=lat;
            longitude=lng;
        }

        if(address.getText().toString().length() < 5 ){
            pd.dismiss();
            Toast.makeText(neworder.this, "Please enter a valid address", Toast.LENGTH_SHORT).show();
        }else{
            String service, time, ecomment, eaddress;
            service = spinner_service.getSelectedItem().toString();
            time = spinner_time.getSelectedItem().toString();
            ecomment = comment.getText().toString();
            eaddress = address.getText().toString();

            Location location=new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            Location loc2=new Location("");
            loc2.setLatitude(26.837474);
            loc2.setLongitude(83.2374646);
            float distance = location.distanceTo(loc2)/1000;

            DecimalFormat df2=new DecimalFormat(".##");
            String dis=""+df2.format(distance)+" "+"Km";
            if(service == "Select service type..."){
                Toast.makeText(neworder.this, "Please select a service type", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
            else{
                if(time == "Select prefered time..."){
                    Toast.makeText(neworder.this, "Please select your prefered time", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
                else {
                    if(addressfound || !address.isEnabled()){
                        //Toast.makeText(neworder.this, "okay"+addressfound, Toast.LENGTH_SHORT).show();
                        //generatecode();
                        generatecodeforqr();
                        String ids=code;
                        String uri=filePath.toString();
                        final Intent intent = new Intent(neworder.this, statuspage.class);
                        intent.putExtra("service", service);
                        intent.putExtra("time", time);
                        intent.putExtra("comment", ecomment);
                        intent.putExtra("location", eaddress);
                        intent.putExtra("heading", servicename);
                        intent.putExtra("lat", latitude);
                        intent.putExtra("lng",longitude);
                        intent.setData(filePath);
                        intent.putExtra("distance",dis);
                        intent.putExtra("code",code);
                        intent.putExtra("codeforqr",codeforqr);
                        intent.putExtra("lastpage","neworder");
                        intent.putExtra("format",format);
                        //intent.putExtra("result", result);
                        pd.dismiss();
                        startActivity(intent);
                        overridePendingTransition(anim.slide_in_right, anim.slide_out_left);

                    }
                    else if(addressfound == false && address.isEnabled()){
                        Toast.makeText(neworder.this, "The address entered was not found", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }}
    }

    private void btnlocationstat(){
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(neworder.this);
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
                    Toast.makeText(neworder.this, "Okay", Toast.LENGTH_SHORT).show();
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
                    locationManager.requestLocationUpdates(provider, 0, 0, neworder.this);

                    address.setText("");
                    showaddress=true;
                    address.setEnabled(false);
                    btnchangeaddress.setVisibility(View.VISIBLE);
                    pd.setMessage("Fetching Location...");
                    pd.show();
                    getloc();

                }
                else {
                    String[] requestloc = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                    requestPermissions(requestloc, REQUEST_LOC_ORDER);
                }
            }
            else {

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(provider, 0, 0, neworder.this);

                address.setText("");
                showaddress=true;
                address.setEnabled(false);
                btnchangeaddress.setVisibility(View.VISIBLE);
                pd.setMessage("Fetching Location...");
                pd.show();
                getloc();
            }
        }
    }

    private void generatecode() {
        final String ALPHA_NUM="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder=new StringBuilder();
        int count=7;
        while (count-- !=0){
            int charecter=(int)(Math.random()*ALPHA_NUM.length());
            builder.append(ALPHA_NUM.charAt(charecter));
            code=builder.toString();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_REQUEST){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                camvideo();
            }
            else {
                Toast.makeText(this, "Camera Permission is needed to take picture", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == REQUEST_LOC_ORDER){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) this);
            }

            else {
                Toast.makeText(this, "Location Permission is required", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == REQ_STORAGE_ORD){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == 1000){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatecodeforqr() {
        final String ALPHA_NUM="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder=new StringBuilder();
        int count=10;
        while (count-- !=0){
            int charecter=(int)(Math.random()*ALPHA_NUM.length());
            builder.append(ALPHA_NUM.charAt(charecter));
            codeforqr=builder.toString();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(anim.slide_in_left, anim.slide_out_right);
    }

    private void requeststorage(){
        String[] reqStorageWrite=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(reqStorageWrite, REQ_STORAGE_WRITE_ORD);
        }
    }

    private void camvideo() {
        final Dialog dialog=new Dialog(neworder.this);
        dialog.setContentView(layout.camorvideo);
        dialog.show();
        Button cam=dialog.findViewById(id.cam);
        Button vid=dialog.findViewById(id.video);
        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                format="image";
                generatecode();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                        File mpath= Environment.getExternalStorageDirectory();
                        File dir=new File(mpath+"/Dots/sent_pics/");
                        dir.mkdirs();
                        String filename=code+".jpg";
                        File file=new File(dir, filename);
                        outputfileuri = FileProvider.getUriForFile(neworder.this, BuildConfig.APPLICATION_ID + ".provider" , file);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputfileuri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(intent, 555);
                        dialog.dismiss();
                    }
                    else {
                        String[] reqStorage=new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(reqStorage, REQ_STORAGE_ORD);
                    }
                }

                else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 555);
                    dialog.dismiss();
                }

            }
        });

        vid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                format="video";
                generatecode();
                /*Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 3);
                startActivityForResult(intent, capture_video);*/
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, 444);
                dialog.dismiss();
            }
        });


    }

    @SuppressLint("NewApi")
    private String getRealPath(Context context, Uri contentUri){
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri,proj,null,null,null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
    }

    private File saveimage(File compressedfile, String name) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(compressedfile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File mpath= Environment.getExternalStorageDirectory();
        File dir=new File(mpath+"/Dots/sent_pics/");
        dir.mkdirs();
        String filename=name+".jpg";
        final File file=new File(dir, filename);

        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try{
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;

    }

    private File savevideo(File compressedfile, String name) {
        File mpath= Environment.getExternalStorageDirectory();
        File dir=new File(mpath+"/Dots/sent_pics/");
        dir.mkdirs();
        String filename=name+".mp4";
        final File file=new File(dir, filename);

        FileOutputStream fos = null;
        try{
            AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(
                    FileProvider.getUriForFile(neworder.this, BuildConfig.APPLICATION_ID + ".provider", compressedfile), "r");
            FileInputStream in = videoAsset.createInputStream();
            fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0 ){
                fos.write(buf,0,len);
            }
            in.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 555 && resultCode == RESULT_OK) {
            filePath=outputfileuri;

            /*String abspath = getRealPath(this, filePath);
            File ff = new File(abspath);*/
            Bitmap bitmap = null;
            //bitmap=(Bitmap)data.getExtras().get("data");
            try {
                bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*File compressedfile = null;
            try {
                compressedfile = new Compressor(this)
                        .setQuality(20)
                        .compressToFile(ff);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //File newfile = saveimage(compressedfile, code);

            btnimage.setVisibility(View.VISIBLE);

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int neww = w / 2;
            int newh = h / 2;

            if (h > w) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap news = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
                btnimage.setImageBitmap(news);
            } else {
                btnimage.setImageBitmap(bitmap);
            }

            btnimage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            btnplay.setVisibility(View.VISIBLE);
            cam_or_vid = "cam";
        }

        if(requestCode == 444 && resultCode == RESULT_OK){

            Uri uri = data.getData();

            String abspath = getRealPath(this, uri);
            File ff = new File(abspath);

            File fl=savevideo(ff,code);

            filePath = FileProvider.getUriForFile(neworder.this, BuildConfig.APPLICATION_ID +".provider" , fl);

            String[] filePathColumn ={MediaStore.Images.Media.DATA};
            Cursor cursor = this.getContentResolver().query(uri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnindex = cursor.getColumnIndex(filePathColumn[0]);
            String picPath=cursor.getString(columnindex);
            cursor.close();
            Bitmap bitmap=ThumbnailUtils.createVideoThumbnail(picPath, MediaStore.Video.Thumbnails.MINI_KIND);

            int w=bitmap.getWidth();
            int h=bitmap.getHeight();
            int neww=w/2;
            int newh=h/2;

            if(h>w){
                Matrix matrix=new Matrix();
                matrix.postRotate(90);
                Bitmap news=Bitmap.createBitmap(bitmap,0,0,w,h,matrix,true);
                btnimage.setImageBitmap(news);
            }else {
                btnimage.setImageBitmap(bitmap);
            }

            btnimage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            btnplay.setVisibility(View.VISIBLE);
            cam_or_vid="vid";

        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        /*lat=location.getLatitude();
        lng=location.getLongitude();
                    Geocoder geo = new Geocoder(neworder.this.getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses.isEmpty()) {
                            address.setText("Waiting for address");
                        } else {
                            if (addresses.size() > 0 && showaddress==true && address.getText().toString().isEmpty()) {
                                String ad = addresses.get(0).getAddressLine(0);
                                address.setText(ad);
                                pd.dismiss();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/


    }

    @SuppressLint("MissingPermission")
    private void getloc(){
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    Geocoder geo = new Geocoder(neworder.this.getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses.isEmpty()) {
                            address.setText("Waiting for address");
                        } else {
                            if (addresses.size() > 0 && showaddress == true && address.getText().toString().isEmpty()) {
                                String ad = addresses.get(0).getAddressLine(0);
                                address.setText(ad);
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
        });
    }

    private File getTempFile(Context context){
        final File path = new File(Environment.getExternalStorageDirectory(),
                context.getPackageName());
        if(!path.exists()){
            path.mkdir();
        }
        return new File(path, "myimage.png");
    }

    private void requestlocation() {
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }

    private void requestcamera() {
        ActivityCompat.requestPermissions(this,new String[]{CAMERA_SERVICE},2);
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(this, "Please enable the GPS", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnplus:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if((checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                        camvideo();
                    }
                    else {
                        String[] pemissionRequest={Manifest.permission.CAMERA};
                        requestPermissions(pemissionRequest, CAMERA_PERMISSION_REQUEST);

                    }
                }
                else {
                    camvideo();
                }
                break;

            case R.id.btnplay:
                if(cam_or_vid.equals("cam")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(filePath, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Open image using"));
                }

                if(cam_or_vid.equals("vid")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(filePath, "video/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Open video using"));
                }
                break;

            case R.id.btnlocation:
                btnlocationstat();
                break;

            case R.id.btnstart:
                submit();
                break;
        }
    }
}
