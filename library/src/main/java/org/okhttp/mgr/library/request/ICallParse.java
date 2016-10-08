package org.okhttp.mgr.library.request;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by huwentao on 16/10/8.
 */
public interface ICallParse<T> {

    T parse(Call call, Response response);
}
