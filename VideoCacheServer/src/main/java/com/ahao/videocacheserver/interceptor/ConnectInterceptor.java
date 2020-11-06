package com.ahao.videocacheserver.interceptor;

import com.ahao.videocacheserver.util.RequestUtil;
import com.ahao.videocacheserver.HttpRequest;
import com.ahao.videocacheserver.HttpResponse;
import com.ahao.videocacheserver.exception.RequestException;

public class ConnectInterceptor implements Interceptor {

    @Override
    public HttpResponse intercept(Chain chain) throws RequestException {
        HttpRequest request = chain.getRequest();
        HttpResponse response = RequestUtil.getHttpResponseFromNet(request);

        if (!response.isOK()) {
            throw new RequestException("request not ok :" + response.getHeadText());
        }
        return response;
    }

}
