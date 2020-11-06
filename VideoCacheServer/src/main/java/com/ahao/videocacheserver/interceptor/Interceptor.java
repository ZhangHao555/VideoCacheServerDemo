package com.ahao.videocacheserver.interceptor;

import com.ahao.videocacheserver.HttpRequest;
import com.ahao.videocacheserver.HttpResponse;
import com.ahao.videocacheserver.exception.RequestException;

public interface Interceptor {
    HttpResponse intercept(Chain chain) throws RequestException;

    interface Chain {
        HttpRequest getRequest();

        HttpResponse proceed(HttpRequest request);
    }
}
