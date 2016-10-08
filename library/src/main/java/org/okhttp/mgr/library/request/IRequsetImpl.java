package org.okhttp.mgr.library.request;

import android.util.Log;

import org.okhttp.mgr.library.exception.HttpException;
import org.okhttp.mgr.library.util.Const;
import org.okhttp.mgr.library.util.HttpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by huwentao on 16/10/8.
 */
public abstract class IRequsetImpl implements IRequest {
    protected String requestUrl;
    protected RequsetComplete requsetComplete;
    protected Headers.Builder headersBuilder = new Headers.Builder();
    protected OkHttpClient okHttpClient;

    private HttpUrl httpUrl;


    public IRequsetImpl(String requestUrl) {
        this.requestUrl = requestUrl;
        this.httpUrl = HttpUrl.parse(requestUrl);
        if (httpUrl.isHttps()) {
            okHttpClient = HttpBuilder.getInstance().getOkHttpsClient();
        } else {
            okHttpClient = HttpBuilder.getInstance().getOkHttpClient();
        }
    }

    /**
     * @param requestUrl
     * @param cookies
     */
    public void addCookie(String requestUrl, List<KeyValue> cookies) {
        HttpUrl httpUrl = HttpUrl.parse(requestUrl);
        Cookie.Builder builder = new Cookie.Builder();
        List<Cookie> cookieList = new ArrayList<>();
        for (KeyValue cookie : cookies) {
            cookieList.add(
                    builder.name(cookie.key).value((String) cookie.value).build()
            );
        }
        okHttpClient.cookieJar().saveFromResponse(httpUrl, cookieList);
    }

    /**
     * @param name
     * @param value
     */
    public void setHeader(String name, String value) {
        removeHeader(name);
        headersBuilder.add(name, value);
    }

    /**
     * @param name
     */
    public void removeHeader(String name) {
        headersBuilder.removeAll(name);
    }

    /**
     * @param httpMethod  请求方式
     * @param requestUrl  请求路径
     * @param requestBody 请求报文
     * @param isSync      是否发送的同步请求
     * @throws HttpException
     */
    @Override
    public void request(HttpMethod httpMethod,
                        String requestUrl, RequestBody requestBody, boolean isSync) throws HttpException {
        this.requestUrl = requestUrl;
        if (!HttpUtil.isIllegalUrl(requestUrl)) {
            Log.d(Const.LOG_TAG, String.format("非法的 url<%s> ", requestUrl));
            throw HttpException.INSTANCE("请求异常");
        }
        Request.Builder builder = new Request.Builder();
        Request request = builder.method(httpMethod.getValue(), requestBody)
                .headers(headersBuilder.build())
                .url(requestUrl)
                .build();
        Call call = okHttpClient.newCall(request);
        if (isSync) {
            try {
                Response response = call.execute();
            } catch (Exception e) {
                throw new HttpException(e);
            }
        } else {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(Const.LOG_TAG, e.getMessage(), e);
                    if (!call.isCanceled() && call.isExecuted()) {
                        call.cancel();
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                }
            });
        }
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public RequsetComplete getRequsetComplete() {
        return requsetComplete;
    }

    public void setRequsetComplete(RequsetComplete requsetComplete) {
        this.requsetComplete = requsetComplete;
    }

    public interface RequsetComplete {
        void onComplete(Call call, Response response);
    }
}
