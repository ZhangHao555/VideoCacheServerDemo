package com.ahao.videocacheserver.interceptor;

import com.ahao.videocacheserver.cache.*;
import com.ahao.videocacheserver.HttpRequest;
import com.ahao.videocacheserver.HttpResponse;
import com.ahao.videocacheserver.exception.RequestException;
import com.ahao.videocacheserver.util.CloseUtil;
import com.ahao.videocacheserver.util.Constant;
import com.ahao.videocacheserver.util.RequestUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CacheInterceptor implements Interceptor {

    private final static Logger logger = Logger.getLogger("CacheInterceptor");
    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("bytes=(\\d*)-(\\d*)");
    private DiskLruCache cache;

    public CacheInterceptor(DiskLruCache cache) {
        this.cache = cache;
    }

    @Override
    public HttpResponse intercept(Chain chain) throws RequestException {
        HttpRequest request = chain.getRequest();
        String host = request.getHost();

        int[] range = {0, -1};
        getRequestRange(range, request);
        HttpResponse cacheHeaders = cache.getCacheHeaders(request.getHost(), request.getUrlWithNoParam());
        if (cacheHeaders == null || cacheHeaders.getTotalLength() == -1) {
            HttpResponse response = refreshUrlHeaders(request);
            if (response == null || !response.isOK()) {
                cache.clearCacheHeaders(request.getHost(), request.getUrlWithNoParam());
                throw new RequestException("Refresh Url Header Failed " + response);
            }
            cache.cacheHeaders(request.getHost(), request.getUrlWithNoParam(), response);
        }

        HttpResponse ch = cache.getCacheHeaders(request.getHost(), request.getUrlWithNoParam());
        int urlTotalLength = ch.getTotalLength();

        if (range[1] == -1) {
            range[1] = urlTotalLength - 1;
        }

        SegmentInfo segmentInfo = new SegmentInfo(host, request.getUrlWithNoParam(), range[0], range[1]);
        List<DiskLruCache.CacheResult> results = cache.get(segmentInfo);

        if (results == null || results.size() == 0) {
            // cache miss
            HttpResponse httpResponse = getHttpResponse(chain, request, segmentInfo, urlTotalLength);
            if (Constant.enableLog) {
                logger.log(Level.INFO, "miss cache \n");
                logger.log(Level.INFO, request.getHeadText());
                logger.log(Level.INFO, "get data from net\n");
                logger.log(Level.INFO, httpResponse.getHeadText());
            }
            return httpResponse;
        }

        if (!results.get(0).isCached()) {
            int missEndBytes = range[0];
            for (DiskLruCache.CacheResult cacheResult : results) {
                if (!cacheResult.isCached()) {
                    missEndBytes += cacheResult.getEndBytes() - cacheResult.getStartBytes() + 1;
                } else {
                    break;
                }
            }

            segmentInfo.setStartByte(range[0]);
            segmentInfo.setEndByte(missEndBytes - 1);

            HttpResponse httpResponse = getHttpResponse(chain, request, segmentInfo, urlTotalLength);
            if (Constant.enableLog) {
                logger.log(Level.INFO, "miss cache \n");
                logger.log(Level.INFO, request.getHeadText());
                logger.log(Level.INFO, "get data from net\n");
                logger.log(Level.INFO, httpResponse.getHeadText());
            }
            return httpResponse;
        }

        // cache hit
        int hitCacheStart = range[0];
        int hitCacheEnd = range[0];
        List<File> cacheFiles = new ArrayList<>();
        int skip = 0;
        for (int i = 0; i < results.size(); i++) {
            DiskLruCache.CacheResult cacheResult = results.get(i);
            if (i == 0) {
                skip = cacheResult.getStartBytes();
            }
            if (cacheResult.isCached()) {
                hitCacheEnd += cacheResult.getEndBytes() - cacheResult.getStartBytes() + 1;
                cacheFiles.add(cacheResult.getCachedFile());
            } else {
                break;
            }
        }

        hitCacheEnd--;

        boolean isPartialContent = hitCacheStart != 0 || hitCacheEnd != urlTotalLength - 1;

        HttpResponse response = cache.getCacheHeaders(segmentInfo.getHost(), segmentInfo.getUrl());
        if (response == null) {
            response = new HttpResponse();
        }
        response.setProtocol(Constant.HTTP_VERSION_1_1);
        response.setStatusCode(isPartialContent ? 206 : 200);
        response.setStatusString(isPartialContent ? Constant.Partial_Content : Constant.OK);
        response.getHeaders().put(Constant.CONNECTION, "close");
        response.getHeaders().put(Constant.CONTENT_LENGTH, hitCacheEnd - hitCacheStart + 1 + "");
        response.getHeaders().put(Constant.ACCEPT_RANGES, "bytes");
        response.getHeaders().put(Constant.CONTENT_RANGE,
                String.format("bytes %d-%d/%d",
                        range[0],
                        hitCacheEnd,
                        urlTotalLength));
        response.setContent(new FilesDataStream(new CommonListFile(cacheFiles), skip, hitCacheEnd - hitCacheStart + 1));
        if (Constant.enableLog) {
            logger.log(Level.INFO, "cache hit : \n");
            logger.log(Level.INFO, response.getHeadText());
        }
        return response;
    }

    private void getRequestRange(int[] range, HttpRequest request) {
        String rangeString = request.getHeaders().get(Constant.RANGE);
        try {
            if (rangeString != null) {
                Matcher matcher = RANGE_HEADER_PATTERN.matcher(rangeString);
                if (matcher.find()) {
                    if (matcher.groupCount() >= 1) {
                        range[0] = Integer.parseInt(matcher.group(1));
                    }
                    if (matcher.groupCount() >= 2) {
                        range[1] = Integer.parseInt(matcher.group(2));
                    }
                }
            }
        } catch (Exception ignored) {
        }

    }

    private HttpResponse getHttpResponse(Chain chain, HttpRequest request, SegmentInfo segmentInfo, int urlTotalLength) throws RequestException {
        int start = segmentInfo.getStartByte();
        int end = segmentInfo.getEndByte();

        if (start == 0 && end == urlTotalLength - 1) {
            request.getHeaders().remove(Constant.RANGE);
        } else {
            request.getHeaders().put(Constant.RANGE, String.format("bytes=%d-%d", start, end));
        }

        HttpResponse proceed = chain.proceed(request);

        if (!proceed.isOK()) {
            throw new RequestException("request is not ok :" + proceed.getHeadText());
        }

        cache.cacheHeaders(request.getHost(), request.getUrlWithNoParam(), proceed);

        BlockListFile blockList = cache.put(segmentInfo, proceed.getContent());
        if (blockList == null) {
            return HttpResponse.get302Response(request.getHost(), request.getUrl(), request.getHostPort());
        }

        proceed.setContent(new FilesDataStream(blockList, blockList.getTotalLength()));
        return proceed;
    }

    private HttpResponse refreshUrlHeaders(HttpRequest request) {
        HttpResponse response = RequestUtil.getHttpResponseFromNet(request);
        if (response == null) {
            return null;
        }
        CloseUtil.close(response.getSocket());
        return response;
    }

}
