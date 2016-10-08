package org.okhttp.mgr.library.util;

import okhttp3.HttpUrl;

/**
 * Created by huwentao on 16/10/8.
 */
public class HttpUtil {
    /**
     * 判断URL是否合法
     *
     * @param url
     * @return
     */
    public static boolean isIllegalUrl(String url) {
        HttpUrl parsed = HttpUrl.parse(url);
        return parsed != null;
    }
}
