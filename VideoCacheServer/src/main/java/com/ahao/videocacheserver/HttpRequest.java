package com.ahao.videocacheserver;

import com.ahao.videocacheserver.util.StringUtil;
import com.ahao.videocacheserver.util.Constant;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest implements Cloneable {
    private String method;
    private String url;
    private String protocol;
    private Map<String, String> headers = new HashMap<>();


    public static HttpRequest parse(InputStream inputStream) {
        HttpRequest request = new HttpRequest();

        StringBuilder sb = new StringBuilder();
        boolean isFirstLine = true;
        try {
            while (true) {
                int charRead = inputStream.read();
                if (charRead == -1) {
                    break;
                }
                sb.append((char) charRead);
                if (sb.charAt(sb.length() - 1) == '\n') {
                    if (sb.length() <= 2) {
                        sb.delete(0, sb.length());
                        break;
                    } else {
                        String headLine = sb.toString().replaceAll("\\r\\n", "");
                        if (isFirstLine) {
                            isFirstLine = false;
                            String[] s = headLine.split(" ");
                            if (s.length == 3) {
                                request.setMethod(StringUtil.trimLR(s[0]));
                                request.setUrl(StringUtil.trimLR(s[1]));
                                request.setProtocol(StringUtil.trimLR(s[2]));
                            }
                        } else {
                            String[] split = headLine.split(":");
                            String key = StringUtil.trimLR(split[0]);
                            String value = StringUtil.trimLR(headLine.substring(headLine.indexOf(key) + key.length() + 1));
                            request.getHeaders().put(key, value);
                        }
                        sb.delete(0, sb.length());

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlWithNoParam() {
        if (url == null || url.length() <= 0) {
            return "";
        }
        int index = url.indexOf("?");
        if (index > 0) {
            return url.substring(0, index);
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return headers.get(Constant.HOST);
    }

    public String setHost(String host) {
        return headers.put(Constant.HOST, host);
    }

    public String getHostPort() {
        return headers.get(Constant.HOST_PORT);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        HttpRequest request = (HttpRequest) super.clone();
        Map<String, String> headers = getHeaders();
        HashMap<String, String> cloneHashMap = new HashMap<>();
        for (Map.Entry<String, String> e : headers.entrySet()) {
            cloneHashMap.put(e.getKey(), e.getValue());
        }
        request.setHeaders(cloneHashMap);
        return request;
    }

    public String getHeadText() {
        StringBuilder text = new StringBuilder(method + " " + url + " " + protocol + "\r\n");
        for (Map.Entry<String, String> e : headers.entrySet()) {
            text.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
        }
        text.append("\r\n");

        return text.toString();
    }
}
