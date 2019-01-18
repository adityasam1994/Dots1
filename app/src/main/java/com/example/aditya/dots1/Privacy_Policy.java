package com.example.aditya.dots1;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

public class Privacy_Policy extends AppCompatActivity {

    ProgressDialog pd;
    ImageView btnback;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy__policy);

        pd=new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Loading...");
        pd.show();
        btnback=(ImageView)findViewById(R.id.btnback);
        webView=(WebView)findViewById(R.id.webView);

        webView.loadUrl("https://privacypolicies.com/privacy/view/fd1bc1ffe8e676b0e5515cd581ea0220");

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
                pd.dismiss();
            }
        });

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}
