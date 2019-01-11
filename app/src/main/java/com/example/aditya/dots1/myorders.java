package com.example.aditya.dots1;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
    String time,oredrpath="",cost,servi;
    Dialog dialog;
    ImageView btnback;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId("AVk4WFQZ8V_ct-PLjK5RrI1yAx6hq4Rt1pAKrPmNJkKAx3QOm1hDpQ-wBrrA-aGuhE7ZSmKZ9a9THHhN");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorders);

        btnback=(ImageView)findViewById(R.id.btnback);
        parent = (LinearLayout) findViewById(R.id.myorders);

        dbr.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                parent.removeAllViews();

                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    String statu = "Pending";

                    String name = ds.getKey().toString();
                    String serv = ds.child("service").getValue().toString();

                    for (DataSnapshot d : ds.getChildren()) {
                        int l = d.getKey().toString().length();

                        if (statu.equals("Rejected")) {
                            statu = "Pending";
                        }

                        if (l > 15) {
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
                                time = ds.child("stopwatch").child("time").getValue().toString();
                                oredrpath=fauth.getCurrentUser().getUid().toString()+"/"+ds.getKey().toString()+"/"+d.getKey().toString();
                                cost=ds.child("cost").getValue().toString();
                                servi=ds.child("service").getValue().toString();
                                break;
                            }


                            if (st.equals("rejected")) {
                                statu = "Rejected";
                                break;
                            }
                        }
                    }

                    final LinearLayout layout = new LinearLayout(myorders.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 90);
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
                                dialog = new Dialog(myorders.this);
                                dialog.setContentView(R.layout.job_done_notification);
                                dialog.show();
                                Button pay = dialog.findViewById(R.id.btnpay);
                                TextView timer = dialog.findViewById(R.id.tvTimer);

                                timer.setText(time);

                                pay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        beginpayment(cost,servi);
                                    }
                                });
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

    private void beginpayment(String cost, String servi) {
        Intent serviceConfig = new Intent(this, PayPalService.class);
        serviceConfig.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(serviceConfig);

        PayPalPayment payment = new PayPalPayment(new BigDecimal(cost),
                "USD", servi, PayPalPayment.PAYMENT_INTENT_SALE);

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
                        dbr.child(oredrpath).child("payment_id").setValue(payment_id);
                        dbr.child(oredrpath).child("create_time").setValue(create_time);
                        dbr.child(oredrpath).child("state").setValue(state);

                        Toast.makeText(this, "Payment was successfull", Toast.LENGTH_SHORT).show();
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
