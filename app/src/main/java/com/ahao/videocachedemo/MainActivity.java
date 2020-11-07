package com.ahao.videocachedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout container = findViewById(R.id.video_container);
        videoView = new VideoView(this);
        container.addView(videoView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        /*"http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4"*/
        String proxyUrl = APP.getVideoProxyServer().getLocalProxyUrl("http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8");
        videoView.setVideoPath(proxyUrl);
        videoView.setMediaController(new MediaController(this));
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.resume();
    }
}