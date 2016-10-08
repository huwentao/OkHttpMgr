package org.okhttp.mgr.library.interceptor;

import android.text.TextUtils;
import android.util.Log;

import org.okhttp.mgr.library.util.Const;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class LoggerInterceptor implements Interceptor {
    private boolean showResponse;

    public LoggerInterceptor(boolean showResponse) {
        this.showResponse = showResponse;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        logForRequest(request);
        Response response = chain.proceed(request);

        return logForResponse(response);
    }

    private void logForRequest(Request request) {
        try {
            String url = request.url().toString();
            Headers headers = request.headers();

            Log.e(Const.LOG_TAG, "---------------------request log start---------------------");
            Log.e(Const.LOG_TAG,"method : " + request.method());
            Log.e(Const.LOG_TAG,"url : " + url);
            if (headers != null && headers.size() > 0) {
                Log.e(Const.LOG_TAG,"headers : \n");
                Log.e(Const.LOG_TAG,headers.toString());
            }
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                if (mediaType != null) {
                    Log.e(Const.LOG_TAG,"contentType : " + mediaType.toString());
                    if (isText(mediaType)) {
                        Log.e(Const.LOG_TAG,"content : " + bodyToString(request));
                    } else {
                        Log.e(Const.LOG_TAG,"content : " + " maybe [file part] , too large too print , ignored!");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(Const.LOG_TAG,e.getMessage(), e);
        } finally {
            Log.e(Const.LOG_TAG,"---------------------request log end-----------------------");
        }
    }

    private Response logForResponse(Response response) {
        try {
            Log.e(Const.LOG_TAG,"---------------------response log start---------------------");
            Response.Builder builder = response.newBuilder();
            Response clone = builder.build();
            Log.e(Const.LOG_TAG,"url : " + clone.request().url());
            Log.e(Const.LOG_TAG,"code : " + clone.code());
            Log.e(Const.LOG_TAG,"protocol : " + clone.protocol());
            if (!TextUtils.isEmpty(clone.message())) Log.e(Const.LOG_TAG,"message : " + clone.message());

            if (showResponse) {
                ResponseBody body = clone.body();
                if (body != null) {
                    MediaType mediaType = body.contentType();
                    if (mediaType != null) {
                        Log.e(Const.LOG_TAG,"contentType : " + mediaType.toString());
                        if (isText(mediaType)) {
                            String resp = body.string();
                            Log.e(Const.LOG_TAG,"content : " + resp);
                            body = ResponseBody.create(mediaType, resp);
                            return response.newBuilder().body(body).build();
                        } else {
                            Log.e(Const.LOG_TAG,"content : " + " maybe [file part] , too large too print , ignored!");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(Const.LOG_TAG,e.getMessage(), e);
        } finally {
            Log.e(Const.LOG_TAG,"---------------------response log end-----------------------");
        }

        return response;
    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.toString().equals("application/x-www-form-urlencoded") ||
                    mediaType.subtype().equals("json") ||
                    mediaType.subtype().equals("xml") ||
                    mediaType.subtype().equals("html") ||
                    mediaType.subtype().equals("webviewhtml")) //
                return true;
        }
        return false;
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "something error when show requestBody.";
        }
    }
}
