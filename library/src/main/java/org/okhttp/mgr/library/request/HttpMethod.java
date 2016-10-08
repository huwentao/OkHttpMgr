package org.okhttp.mgr.library.request;

/**
 * Created by huwentao on 16/10/8.
 */
public enum HttpMethod {
    GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE");
    private String mMethod;

    HttpMethod(String method) {
        this.mMethod = method;
    }

    public String getValue() {
        return mMethod;
    }
}
