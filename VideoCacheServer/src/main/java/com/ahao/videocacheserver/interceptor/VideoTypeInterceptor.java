package com.ahao.videocacheserver.interceptor;

import com.ahao.videocacheserver.HttpRequest;
import com.ahao.videocacheserver.ProxyCharset;
import com.ahao.videocacheserver.exception.RequestException;
import com.ahao.videocacheserver.util.CloseUtil;
import com.ahao.videocacheserver.util.Constant;
import com.ahao.videocacheserver.HttpResponse;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class VideoTypeInterceptor implements Interceptor {

    private final static List<String> M3U8_CONTENT_TYPES = Arrays.asList("vnd.apple.mpegurl", "application/x-mpegurl");

    @Override
    public HttpResponse intercept(Chain chain) throws RequestException {
        HttpRequest request = chain.getRequest();
        HttpResponse response = chain.proceed(request);

        String urlWithNoParam = request.getUrlWithNoParam();
        String contentType = response.getHeaders().get(Constant.CONTENT_TYPE);
        if (contentType != null) {
            contentType = contentType.toLowerCase();
        }
        boolean isM3U8 = urlWithNoParam.toLowerCase().endsWith("m3u8") || M3U8_CONTENT_TYPES.contains(contentType);

        try {
            if (isM3U8) {
                String realHost = request.getHost();
                String hostPort = request.getHostPort();
                if (hostPort == null) {
                    hostPort = "80";
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream content = response.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                boolean isTsUrl = false;

                byte[] enter = "\r\n".getBytes(ProxyCharset.CUR_CHARSET);


                while ((line = reader.readLine()) != null) {
                    byte[] lineBytes = line.getBytes(ProxyCharset.CUR_CHARSET);
                    baos.write(lineBytes);

                    if (isTsUrl) {
                        String param = String.format("%s%s=%s:%s", line.contains("?") ? "&" : "?", Constant.REAL_HOST_NAME, realHost, hostPort);
                        baos.write(param.getBytes(ProxyCharset.CUR_CHARSET));
                        isTsUrl = false;
                    }

                    if (line.contains("#EXTINF")) {
                        isTsUrl = true;
                    }
                    baos.write(enter);
                }

                baos.flush();
                response.setContent(new ByteArrayInputStream(baos.toByteArray()));
                response.setContentLength(baos.size());
                response.getHeaders().put(Constant.CONTENT_RANGE, String.format("%d-%d/%d", 0, baos.size() - 1, baos.size()));

                CloseUtil.close(reader);
            }
        } catch (Exception e) {
            throw new RequestException(e.getMessage());
        }
       return response;
    }


}
