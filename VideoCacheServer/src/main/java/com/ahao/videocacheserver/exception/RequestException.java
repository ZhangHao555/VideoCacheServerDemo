package com.ahao.videocacheserver.exception;

public class RequestException extends Exception{
    private String message;

    public RequestException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
