package com.example.aditya.dots1;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

public class Video_player extends AppCompatActivity {

    Uri uri;
    VideoView videoView;
    Button btnplay,btnpause;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView=(VideoView)findViewById(R.id.videoView);
        btnpause=(Button)findViewById(R.id.btnpause);
        btnplay=(Button)findViewById(R.id.btnplay);

        uri=Uri.parse(getIntent().getData().toString());
        videoView.setVideoURI(uri);

        Toast.makeText(this, getIntent().getData().toString(), Toast.LENGTH_SHORT).show();
        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.start();
            }
        });

        btnpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.pause();
            }
        });

    }
}
