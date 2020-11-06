package com.ahao.videocachedemo;

import android.app.Application;
import android.util.Log;

import com.ahao.videocacheserver.VideoCacheServer;
import com.ahao.videocacheserver.util.Constant;


public class APP extends Application {

    public static String TAG = "APP";
    private static VideoCacheServer videoCacheServer;

    @Override
    public void onCreate() {
        super.onCreate();

        if (videoCacheServer == null) {
            String cachePath = getCacheDir().getAbsolutePath();
            videoCacheServer = new VideoCacheServer(cachePath, 1024 * 1024 * 500);
            Constant.enableLog = true;
            int port = videoCacheServer.start();
            Log.i(TAG, "onCreate: start video proxy Server at " + port);
        }
    }

    public static VideoCacheServer getVideoProxyServer() {
        return videoCacheServer;
    }

}
