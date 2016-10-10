package org.okhttp.mgr.library.request;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by huwentao on 16/10/8.
 */
public interface ICallParse<T> {

    T parse(boolean isSuccess, Call call, Response response,IOException e);
}
