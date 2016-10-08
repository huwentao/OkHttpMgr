package org.okhttp.mgr.library.request;

import org.okhttp.mgr.library.exception.HttpException;

import okhttp3.RequestBody;

/**
 * Created by huwentao on 16/10/8.
 */
public interface IRequest {
    /**
     * 发送网络请求
     *
     * @param httpMethod  请求方式
     * @param requestUrl  请求路径
     * @param requestBody 请求报文
     * @param isSync      是否发送的同步请求
     * @throws HttpException 网络请求异常
     */
    void request(HttpMethod httpMethod,
                 String requestUrl,
                 RequestBody requestBody,
                 boolean isSync) throws HttpException;
}
