package com.example.aditya.dots1;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class myorders extends AppCompatActivity {

    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Orders");
    FirebaseAuth fauth = FirebaseAuth.getInstance();
    LinearLayout parent;
    String time,oredrpath="",cost,servi,state;
    Dialog dialog;
    ImageView btnback;
    SharedPreferences spref;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId("AVk4WFQZ8V_ct-PLjK5RrI1yAx6hq4Rt1pAKrPmNJkKAx3QOm1hDpQ-wBrrA-aGuhE7ZSmKZ9a9THHhN");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorders);

        spref=getSharedPreferences("payment", Context.MODE_PRIVATE);
        btnback=(ImageView)findViewById(R.id.btnback);
        parent = (LinearLayout) findViewById(R.id.myorders);

        dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                parent.removeAllViews();

                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    String statu = "Pending";
                    int count = 0;
                    String name = ds.getKey().toString();
                    String serv = ds.child("service").getValue().toString();

                    for (DataSnapshot d : ds.getChildren()) {
                        int l = d.getKey().toString().length();

                        if (ds.hasChild("provider_found")) {
                            if (ds.child("provider_found").getValue().toString().equals("false")) {
                                statu = "Not Found";
                                break;
                            }
                        }
                        else {
                            if (l > 15) {

                                count = count+1;

                                if (statu.equals("Rejected")) {
                                    statu = "Pending";
                                }

                                String st = d.child("status").getValue().toString();
                                if (st.equals("accepted")) {
                                    statu = "Accepted";
                                    break;
                                }

                                if (st.equals("cancelled")) {
                                    statu = "Cancelled";
                                    break;
                                }

                                if (st.equals("pending")) {
                                    statu = "Pending";
                                    break;
                                }
                                if (st.equals("completed")) {
                                    statu = "Completed";
                                    break;
                                }
                                if (st.equals("rejected")) {
                                    statu = "Rejected";
                                }
                            }
                        }
                    }

                    float dip_h=50f;
                    Resources r=getResources();
                    float px_h= TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, dip_h, r.getDisplayMetrics()
                    );

                    if(count == 0){
                        statu = "Not Found";
                    }

                    final LinearLayout layout = new LinearLayout(myorders.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) px_h);
                    params.setMargins(0, 20, 0, 0);
                    layout.setLayoutParams(params);
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.setBackground(ContextCompat.getDrawable(myorders.this, R.drawable.t_stripe));


                    TextView service = new TextView(myorders.this);
                    service.setText(name);
                    service.setTextSize(18);
                    service.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    service.setLayoutParams(para);

                    final TextView order1 = new TextView(myorders.this);
                    order1.setText(serv);
                    order1.setTextSize(18);
                    order1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                    order1.setTypeface(null, Typeface.BOLD);
                    order1.setBackground(ContextCompat.getDrawable(myorders.this, R.drawable.commentborder));
                    LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    order1.setLayoutParams(par);

                    TextView status = new TextView(myorders.this);
                    status.setText(statu);
                    status.setTextSize(18);
                    status.setGravity(Gravity.CENTER);
                    status.setLayoutParams(para);

                    layout.addView(service);
                    layout.addView(order1);
                    layout.addView(status);
                    parent.addView(layout);

                    final String finalStatu = statu;
                    layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(finalStatu.equals("Not Found")){
                                Toast.makeText(myorders.this, "Sorry! no provider is available in your area", Toast.LENGTH_SHORT).show();
                            }

                            if (finalStatu.equals("Accepted")) {
                                Intent intent = new Intent(myorders.this, order_status_service.class);
                                intent.putExtra("oid", ds.getKey().toString());
                                intent.putExtra("lastpage", "myorders");
                                startService(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }

                            if(finalStatu.equals("Cancelled")){
                                Toast.makeText(myorders.this, "This order was cancelled by you", Toast.LENGTH_SHORT).show();
                            }

                            if (finalStatu.equals("Pending")) {
                                Intent intent = new Intent(myorders.this, pending_order.class);
                                intent.putExtra("oid", ds.getKey().toString());
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                            if (finalStatu.equals("Rejected")) {
                                Toast.makeText(myorders.this, "No provider was found for this order", Toast.LENGTH_SHORT).show();
                            }
                            if (finalStatu.equals("Completed")) {

                                final String[] ordpath=new String[1];
                                final String[] stat=new String[1];

                                for(DataSnapshot dd : ds.getChildren()){
                                    if(dd.getKey().toString().length() > 20){
                                        stat[0] = dd.child("state").getValue().toString();
                                        ordpath[0]=fauth.getCurrentUser().getUid().toString()+"/"+ds.getKey().toString()+"/"+dd.getKey().toString();
                                    }
                                }
                                    if (stat[0].equals("approved")) {
                                        Toast.makeText(myorders.this, "This order has been completed and paid", Toast.LENGTH_SHORT).show();
                                    } else {
                                        dialog = new Dialog(myorders.this);
                                        dialog.setContentView(R.layout.job_done_notification);
                                        dialog.show();

                                        Button pay = dialog.findViewById(R.id.btnpay);
                                        final TextView timer = dialog.findViewById(R.id.tvTimer);
                                        final TextView service = dialog.findViewById(R.id.tvservicename);
                                        final TextView tvcost = dialog.findViewById(R.id.tvcost);

                                        final String oid = ds.getKey().toString();
                                        final String[] ser = new String[1];
                                        final String[] cos = new String[1];
                                        final String[] ti = new String[1];

                                        ser[0] =ds.child("service").getValue().toString();
                                        cos[0] =ds.child("cost").getValue().toString();
                                        ti[0] =ds.child("stopwatch").child("time").getValue().toString();

                                        tvcost.setText(cos[0] + "$");
                                        timer.setText(ti[0]);
                                        service.setText("(" + oid + ") " + ser[0] + ":");

                                        pay.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                beginpayment(cos[0], ser[0], ordpath[0]);
                                            }
                                        });
                                    }
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void beginpayment(String cost, String servi, String ordpath) {
        dialog.dismiss();
        Intent serviceConfig = new Intent(this, PayPalService.class);
        serviceConfig.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(serviceConfig);

        PayPalPayment payment = new PayPalPayment(new BigDecimal(cost),
                "USD", servi, PayPalPayment.PAYMENT_INTENT_SALE);

        spref.edit().putString("ordpath", ordpath).commit();

        Intent paymentConfig = new Intent(this, PaymentActivity.class);
        paymentConfig.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        paymentConfig.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(paymentConfig, 0);
    }

    @Override
    public void onDestroy(){
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        final String orpath=spref.getString("ordpath", "");

        if (resultCode == Activity.RESULT_OK){
            PaymentConfirmation confirm = data.getParcelableExtra(
                    PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null){
                try {
                    Log.i("sampleapp", confirm.toJSONObject().toString(4));

                    JSONObject payment=confirm.toJSONObject();
                    JSONObject response=payment.getJSONObject("response");

                    String payment_id=response.getString("id");
                    String create_time=response.getString("create_time");
                    String state=response.getString("state");

                    if(state.equals("approved")) {
                        dbr.child(orpath).child("payment_id").setValue(payment_id);
                        dbr.child(orpath).child("create_time").setValue(create_time);
                        dbr.child(orpath).child("state").setValue(state);

                        Intent intent=new Intent(myorders.this, rating.class);
                        intent.putExtra("orderpath", orpath);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(this, "Payment was not approved!", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Log.e("sampleapp", "no confirmation data: ", e);
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("sampleapp", "The user canceled.");
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i("sampleapp", "Invalid payment / config set");
        }
    }
}
