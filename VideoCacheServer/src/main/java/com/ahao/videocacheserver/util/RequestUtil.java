package com.ahao.videocacheserver.util;


import com.ahao.videocacheserver.HttpRequest;
import com.ahao.videocacheserver.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestUtil {
    public static HttpResponse getHttpResponseFromNet(HttpRequest request) {
        Socket socket;
        HttpResponse response = null;
        HttpRequest re = request;
        boolean redirect;
        do {
            try {
                int port = 80;
                try {
                    port = Integer.parseInt(re.getHeaders().get(Constant.HOST_PORT));
                } catch (Exception ignored) {
                }
                socket = new Socket(re.getHost(), port);
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(re.getHeadText().getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                response = HttpResponse.parse(inputStream);
                response.setSocket(socket);

                redirect = response.getStatusCode() == 301 || response.getStatusCode() == 302 || response.getStatusCode() == 303;

                if (redirect) {
                    String location = response.getHeaders().get(Constant.LOCATION);
                    if (!StringUtil.isEmpty(location)) {
                        URL url = new URL(location);
                        re = new HttpRequest();
                        re.setMethod(Constant.METHOD_GET);
                        re.setProtocol(Constant.HTTP_VERSION_1_1);
                        re.setUrl(url.getPath());
                        re.setHost(url.getHost());
                        int redirectPort = url.getPort();
                        if (redirectPort == -1) {
                            redirectPort = 80;
                        }
                        re.getHeaders().put(Constant.HOST_PORT, String.valueOf(redirectPort));
                    }
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        } while (redirect);

        return response;
    }

    public static String getRealHostNameWithPort(HttpRequest request) {
        String url = request.getUrl();
        Matcher matcher = Pattern.compile(Constant.REAL_HOST_NAME + "=([^&]*)").matcher(url);

        if (matcher.find()) {
            String realHostName = matcher.group(1);
            int realHostPort = 80;
            if (realHostName.contains(":")) {
                String[] split = realHostName.split(":");
                realHostName = split[0];
                realHostPort = Integer.parseInt(split[1]);
            }
            return realHostName + ":" + realHostPort;
        }
        return null;
    }
}
