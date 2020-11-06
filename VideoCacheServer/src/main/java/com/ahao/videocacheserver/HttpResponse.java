package com.ahao.videocacheserver;

import com.ahao.videocacheserver.util.Constant;
import com.ahao.videocacheserver.util.StringUtil;

import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode;
    private String statusString;
    private String protocol;
    private Map<String, String> headers = new HashMap<>();
    private InputStream content;

    private Socket socket;

    public static HttpResponse parse(InputStream inputStream) {
        HttpResponse response = new HttpResponse();
        response.setContent(inputStream);

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
                            if (s.length >= 2) {
                                response.setProtocol(StringUtil.trimLR(s[0]));
                                response.setStatusCode(Integer.parseInt(StringUtil.trimLR(s[1])));
                                String statusString = headLine.substring(headLine.indexOf(s[1]) + s[1].length() + 1);
                                response.setStatusString(StringUtil.trimLR(statusString));
                            }
                        } else {
                            String[] split = headLine.split(":");
                            String key = StringUtil.trimLR(split[0].trim());
                            String value = StringUtil.trimLR(headLine.substring(headLine.indexOf(key) + key.length() + 1));
                            response.getHeaders().put(key, value);

                        }
                        sb.delete(0, sb.length());

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public InputStream getContent() {
        return content;
    }

    public long getContentLength() {
        return Long.parseLong(getHeaders().get(Constant.CONTENT_LENGTH));
    }

    public void setContentLength(long contentLength) {
        getHeaders().put(Constant.CONTENT_LENGTH, String.valueOf(contentLength));
    }

    public int getTotalLength() {
        try {
            String range = getHeaders().get(Constant.CONTENT_RANGE);
            if (range == null) {
                return Integer.parseInt(getHeaders().get(Constant.CONTENT_LENGTH));
            }
            int i = range.indexOf("/");
            if (i == -1) {
                return -1;
            }
            return Integer.parseInt(range.substring(i + 1));
        } catch (Exception e) {
            return -1;
        }
    }

    public String getHeadText() {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append(" ").append(statusCode).append(" ").append(statusString).append("\r\n");
        for (Map.Entry<String, String> e : headers.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public static HttpResponse get302Response(String redirectHost, String redirectPort, String redirectUrl) {
        String url = String.format("http://%s:%s%s", redirectHost, redirectPort, redirectUrl);
        HttpResponse response = new HttpResponse();
        response.setStatusCode(302);
        response.setStatusString("Temporary Redirect");
        response.setProtocol(Constant.HTTP_VERSION_1_1);
        response.getHeaders().put(Constant.LOCATION, url);
        return response;
    }

    public static HttpResponse get404Response() {
        HttpResponse response = new HttpResponse();
        response.setProtocol(Constant.HTTP_VERSION_1_1);
        response.setStatusCode(404);
        response.setStatusString("NoProxyHost");
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Constant.CONNECTION, "close");
        return response;
    }

    @Override
    public String toString() {
        return getHeadText();
    }

    public boolean isOK() {
        return statusCode < 300 && statusCode >= 200;
    }
}
