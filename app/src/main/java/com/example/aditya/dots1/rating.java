package com.example.aditya.dots1;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class rating extends AppCompatActivity {

    Button btnrating;
    RatingBar ratingBar;
    String orderpath, providerid, profilepic;
    DatabaseReference dbruser;
    Uri profile;
    Boolean piclink=false;
    ImageView providerpic;
    DatabaseReference dbrorder;
    StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl("gs://dots-195d9.appspot.com");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        Toast.makeText(this, "Payment was successfull", Toast.LENGTH_SHORT).show();

        providerpic=(ImageView)findViewById(R.id.profilepic);
        orderpath=getIntent().getExtras().getString("orderpath");
        btnrating=(Button)findViewById(R.id.btnrating);
        ratingBar=(RatingBar)findViewById(R.id.ratingbar);
        dbrorder= FirebaseDatabase.getInstance().getReference("Orders");
        dbruser=FirebaseDatabase.getInstance().getReference("Users");

        //Toast.makeText(this, ""+orderpath, Toast.LENGTH_SHORT).show();

        dbrorder.child(orderpath).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    providerid=dataSnapshot.getKey().toString();

                    dbruser.child(providerid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String fname=dataSnapshot.child("fname").getValue().toString();
                            String lname=dataSnapshot.child("lname").getValue().toString();
                            String service=dataSnapshot.child("info").child("eservice").getValue().toString();

                            if(dataSnapshot.hasChild("profilepic")) {
                                piclink = true;
                                profilepic = dataSnapshot.child("profilepic").getValue().toString();
                                Picasso.get().load(profilepic).resize(100, 100).into(providerpic);
                            }

                            else {
                                storageReference.child("images/"+providerid).getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Picasso.get().load(uri).resize(100, 100).into(providerpic);

                                            }
                                        });
                            }

                            String name=fname+" "+lname;
                            TextView tvname=(TextView)findViewById(R.id.tvprovidername);
                            TextView serv=(TextView)findViewById(R.id.tvproviderservice);

                            tvname.setText(name);
                            serv.setText(service);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnrating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbrorder.child(orderpath).child("rating").setValue(""+ratingBar.getRating());

                Toast.makeText(rating.this, "Thanks for the rating", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(rating.this, newdrawer.class));
            }
        });

    }
}
