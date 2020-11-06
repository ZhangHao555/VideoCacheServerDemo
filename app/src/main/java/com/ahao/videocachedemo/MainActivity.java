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
        /*http://192.168.2.197/m3u8/xiaochou.m3u8*/
        /*https://sina.com-h-sina.com/20181024/21342_8f737b71/1000k/hls/index.m3u8*/
        String proxyUrl = APP.getVideoProxyServer().getLocalProxyUrl("https://sina.com-h-sina.com/20181024/21342_8f737b71/1000k/hls/index.m3u8");
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