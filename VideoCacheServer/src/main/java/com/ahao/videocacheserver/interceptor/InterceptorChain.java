package com.ahao.videocacheserver.interceptor;

import com.ahao.videocacheserver.HttpRequest;
import com.ahao.videocacheserver.exception.RequestException;
import com.ahao.videocacheserver.util.RequestUtil;
import com.ahao.videocacheserver.HttpResponse;

import java.util.List;

public class InterceptorChain implements Interceptor.Chain {
    private List<Interceptor> interceptors;
    private HttpRequest request;
    private int index;

    public InterceptorChain(List<Interceptor> interceptorList, HttpRequest request, int index) {
        this.interceptors = interceptorList;
        this.request = request;
        this.index = index;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public HttpResponse proceed(HttpRequest request) {
        if (index >= interceptors.size()) {
            throw new AssertionError();
        }
        InterceptorChain next = new InterceptorChain(interceptors, request, index + 1);
        Interceptor interceptor = interceptors.get(index);
        try {
            return interceptor.intercept(next);
        } catch (RequestException e) {
            String realHost = null;
            String realPort = null;
            String realHostNameWithPort = RequestUtil.getRealHostNameWithPort(request);
            if (realHostNameWithPort != null) {
                String[] split = realHostNameWithPort.split(":");
                if (split.length == 2) {
                    realHost = split[0];
                    realPort = split[1];
                }
            } else {
                realHost = request.getHost();
                realPort = request.getHostPort();
            }

            if (realHost == null || realPort == null) {
                return HttpResponse.get404Response();
            } else {
                return HttpResponse.get302Response(realHost, realPort, request.getUrl());
            }

        }
    }
}
