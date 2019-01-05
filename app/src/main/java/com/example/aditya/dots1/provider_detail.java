package com.example.aditya.dots1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class provider_detail extends AppCompatActivity implements LocationListener {

    Spinner service, avilable;
    EditText address, comment, age;
    TextView uname;
    Button save, btngetloc;
    ProgressDialog pd;
    FirebaseAuth fauth = FirebaseAuth.getInstance();
    DatabaseReference dbr, dbruser;
    LocationManager locationManager;
    double lat, lng;
    Boolean showaddress=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_detail);

        btngetloc = (Button) findViewById(R.id.btngetloc);
        uname = (TextView) findViewById(R.id.tvname);
        age = (EditText) findViewById(R.id.etage);
        address = (EditText) findViewById(R.id.etlocation);
        comment = (EditText) findViewById(R.id.etcomment);
        save = (Button) findViewById(R.id.btnsave);
        service = (Spinner) findViewById(R.id.spinnerservice);
        avilable = (Spinner) findViewById(R.id.spinneravil);
        pd = new ProgressDialog(this);

        //Toast.makeText(this, fauth.getCurrentUser().getUid().toString(), Toast.LENGTH_SHORT).show();

        dbr = FirebaseDatabase.getInstance().getReference("Provider");
        dbruser = FirebaseDatabase.getInstance().getReference("Users");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) this);

        dbruser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fname=dataSnapshot.child(fauth.getCurrentUser().getUid()).child("fname").getValue().toString();
                uname.setText(fname);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ArrayList<String> list=new ArrayList<>();
        list.add("Select the service type...");
        list.add("Lifting");
        list.add("Plumbing");
        list.add("Electric");

        ArrayList<String> listtime=new ArrayList<>();

        listtime.add("Select time avilable...");
        listtime.add("8am-11am");
        listtime.add("11am-2pm");
        listtime.add("2pm-5pm");
        listtime.add("5pm-8pm");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, list);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, listtime);

        service.setAdapter(adapter);
        adapter.setDropDownViewResource(R.layout.activity_spinner_item);

        avilable.setAdapter(adapter1);
        adapter1.setDropDownViewResource(R.layout.activity_spinner_item);

        service.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        avilable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.setMessage("Saving Details...");
                pd.show();
                double lati=0, longi=0;

                if(address.isEnabled()) {
                    Geocoder geocoder=new Geocoder(provider_detail.this.getApplicationContext(),Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses=geocoder.getFromLocationName(address.getText().toString(),1);
                        if(addresses.size()>0){
                            lati=addresses.get(0).getLatitude();
                            longi=addresses.get(0).getLongitude();
                        }
                        else {
                            Toast.makeText(provider_detail.this, "Address not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    lati=lat;
                    longi=lng;
                }

                String eaddress,eservice,eage,eavilable,ecomment;
                eaddress=address.getText().toString().trim();
                eservice=service.getSelectedItem().toString();
                eage=age.getText().toString().trim();
                eavilable=avilable.getSelectedItem().toString();
                ecomment=comment.getText().toString().trim();
                provider_info pinfo=new provider_info(eservice,eavilable,eage,eaddress,ecomment,lati,longi);

                dbruser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("info").setValue(pinfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(provider_detail.this, "Details saved", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                        startActivity(new Intent(provider_detail.this, provider_home.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(provider_detail.this, "Failed to save details "+e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btngetloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(provider_detail.this);
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
                            Toast.makeText(provider_detail.this, "Okay", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();
                }
                else{
                    address.setText("");
                    showaddress=true;
                    address.setEnabled(false);
                    pd.setMessage("Fetching Location...");
                    pd.show();
                }
            }
        });


    }

    @Override
    public void onLocationChanged(Location location) {
        lat=location.getLatitude();
        lng=location.getLongitude();
        Geocoder geo = new Geocoder(provider_detail.this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.isEmpty()) {
                address.setText("Waiting for address");
            } else {
                if (addresses.size() > 0 && showaddress==true && address.getText().toString().isEmpty()) {
                    String ad = addresses.get(0).getFeatureName() + ","
                            + addresses.get(0).getLocality() + ","
                            + addresses.get(0).getAdminArea() + ","
                            + addresses.get(0).getCountryName() + ","
                            + addresses.get(0).getPostalCode();
                    address.setText(ad);
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
        Toast.makeText(this, "Location provider is diasabled!", Toast.LENGTH_SHORT).show();
    }
}
