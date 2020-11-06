package com.ahao.videocacheserver.cache;

import java.util.Objects;

public class SegmentInfo {
    private String url;
    private int startByte;
    private int endByte;
    private String host;

    public SegmentInfo(String host, String url, int startByte, int endByte) {
        this.host = host;
        this.url = url;
        this.startByte = startByte;
        this.endByte = endByte;
    }


    @Override
    public String toString() {
        return "SegmentInfo{" +
                "url='" + url + '\'' +
                ", startByte=" + startByte +
                ", endByte=" + endByte +
                ", host='" + host + '\'' +
                '}';
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStartByte() {
        return startByte;
    }

    public void setStartByte(int startByte) {
        this.startByte = startByte;
    }

    public int getEndByte() {
        return endByte;
    }

    public void setEndByte(int endByte) {
        this.endByte = endByte;
    }

    public String getUrl() {
        return url;
    }

    public int getLength() {
        return endByte - startByte + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SegmentInfo that = (SegmentInfo) o;
        return startByte == that.startByte &&
                endByte == that.endByte &&
                Objects.equals(url, that.url) &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, url, startByte, endByte);
    }
}
