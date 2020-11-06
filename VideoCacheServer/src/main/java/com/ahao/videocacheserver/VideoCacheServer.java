package com.ahao.videocacheserver;

import com.ahao.videocacheserver.interceptor.*;
import com.ahao.videocacheserver.util.CustomURLEncode;
import com.ahao.videocacheserver.cache.DiskLruCache;
import com.ahao.videocacheserver.util.CloseUtil;
import com.ahao.videocacheserver.util.Constant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoCacheServer {

    private static final Logger logger = Logger.getLogger("VideoCacheServer");
    private DiskLruCache diskCache;
    private ExecutorService singleService = Executors.newSingleThreadExecutor();
    private ExecutorService pool = Executors.newFixedThreadPool(20);
    private boolean isRunning = false;
    private int curPort;

    public VideoCacheServer(String cachePath, int maxCacheSize) {
        diskCache = new DiskLruCache(cachePath, maxCacheSize, 1024 * 1024 * 20);
    }

    public int start() {
        if (isRunning) {
            return curPort;
        }
        curPort = new Random().nextInt(65535);
        try {
            final ServerSocket server = new ServerSocket(curPort);
            isRunning = true;
            singleService.submit(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            Socket connection = server.accept();
                            connection.setKeepAlive(true);
                            pool.submit(new ProxyHandler(connection));
                        } catch (IOException ex) {
                            if (Constant.enableLog) {
                                logger.log(Level.WARNING, "Exception accepting connection", ex);
                            }
                        } catch (Exception ex) {
                            if (Constant.enableLog) {
                                logger.log(Level.SEVERE, "Unexpected error", ex);
                            }
                        }
                    }
                }
            });
            return curPort;
        } catch (IOException e) {
            e.printStackTrace();
            return start();
        }
    }

    public static String getProxyUrl(String url, String proxyAddr, int port) {
        try {
            URL u = new URL(url);
            String realHost = u.getHost();
            int realPort = u.getPort();
            if (realPort == -1) {
                realPort = 80;
            }
            String proxyHostPort = proxyAddr + ":" + port;
            url = url.replace("https", "http");

            if (url.contains(realHost + ":" + realPort)) {
                url = url.replace(realHost + ":" + realPort, proxyHostPort);
            } else {
                url = url.replace(realHost, proxyHostPort);
            }
            return url + ((url.contains("?") ? "&" : "?") + Constant.REAL_HOST_NAME + "=" + realHost + ":" + realPort);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getLocalProxyUrl(String url) {
        String encodeUrl = CustomURLEncode.urlEncode(url);
        try {
            String proxyAddr = "127.0.0.1";
            URL u = new URL(encodeUrl);
            String realHost = u.getHost();
            int realPort = u.getPort();
            if (realPort == -1) {
                realPort = 80;
            }
            String proxyHostPort = proxyAddr + ":" + curPort;
            encodeUrl = encodeUrl.replace("https", "http");

            if (encodeUrl.contains(realHost + ":" + realPort)) {
                encodeUrl = encodeUrl.replace(realHost + ":" + realPort, proxyHostPort);
            } else {
                encodeUrl = encodeUrl.replace(realHost, proxyHostPort);
            }
            return encodeUrl + ((encodeUrl.contains("?") ? "&" : "?") + Constant.REAL_HOST_NAME + "=" + realHost + ":" + realPort);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void destory() {
        isRunning = false;
    }

    public class ProxyHandler implements Runnable {

        private Socket realClientSocket;

        ProxyHandler(Socket realClientSocket) {
            this.realClientSocket = realClientSocket;
        }

        @Override
        public void run() {
            try {
                BufferedOutputStream outputStream = new BufferedOutputStream(realClientSocket.getOutputStream());
                BufferedInputStream inputStream = new BufferedInputStream(realClientSocket.getInputStream());
                HttpRequest realRequest = HttpRequest.parse(inputStream);
                HttpResponse response = getResponseWithInterceptorChain(realRequest);
                writeResponseAndClose(response, outputStream);

            } catch (Exception e) {
                if (Constant.enableLog) {
                    logger.log(Level.SEVERE, "error proxy ", e);
                }
            } finally {
                CloseUtil.close(realClientSocket);
            }
        }

        private HttpResponse getResponseWithInterceptorChain(HttpRequest realRequest) {
            List<Interceptor> interceptors = new ArrayList<>();
            interceptors.add(new VideoTypeInterceptor());
            interceptors.add(new HostFilterInterceptor(curPort));
            interceptors.add(new CacheInterceptor(diskCache));
            interceptors.add(new ConnectInterceptor());
            InterceptorChain interceptorChain = new InterceptorChain(interceptors, realRequest, 0);
            return interceptorChain.proceed(realRequest);
        }

        private void writeResponseAndClose(HttpResponse response, BufferedOutputStream outputStream) {
            try {
                outputStream.write(response.getHeadText().getBytes(ProxyCharset.CUR_CHARSET));

                InputStream content = response.getContent();
                if (content != null) {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(content);
                    byte[] buf = new byte[1024 * 512];
                    int readLength;

                    while ((readLength = bufferedInputStream.read(buf, 0, buf.length)) != -1) {
                        outputStream.write(buf, 0, readLength);
                    }
                    outputStream.flush();
                } else {
                    outputStream.write("\r\n".getBytes(ProxyCharset.CUR_CHARSET));
                }
            } catch (IOException e) {
                if (Constant.enableLog) {
                    logger.log(Level.SEVERE, "response to real server error ", e);
                }
            } finally {
                CloseUtil.close(outputStream);
                CloseUtil.close(response.getContent());
                CloseUtil.close(response.getSocket());
            }

        }

    }
/*
    public static void main(String[] args) {
        VideoCacheServer videoProxyServer = new VideoCacheServer("D:\\cache", 1024 * 1024 * 500);
        int start = videoProxyServer.start();
        String proxyUrl = videoProxyServer.getLocalProxyUrl("https://qotest.qsxt.info/live_record_186_1590149574858.mp4");
        System.out.println(proxyUrl);
    }*/

}
