package com.ahao.videocacheserver.util;

public class Constant {
    public static final String REAL_HOST_NAME = "RealHostParam";

    public static final String CONTENT_RANGE = "Content-Range";
    public static final String RANGE = "Range";
    public static final String CONNECTION = "Connection";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT_RANGES = "Accept-Ranges";
    public static final String Partial_Content = "Partial Content";
    public static final String OK = "OK";
    public static final String HOST_PORT = "RealHostPort";
    public static final String PROXY_HOST = "ProxyHost";
    public static final String HOST = "Host";
    public static final String IF_RANGE = "If-Range";

    public static final String HTTP_VERSION_1_1 = "HTTP/1.1";
    public static final String LOCATION = "Location";
    public static final String REFERER = "Referer";
    public static final String METHOD_GET = "GET";

    public static boolean enableLog = false;

    public static final int CACHE_SLICE_5MB = 1024 * 1024 * 5;
    public static final int CACHE_SLICE_10MB = 1024 * 1024 * 10;
    public static final int CACHE_SLICE_20MB = 1024 * 1024 * 20;

}
