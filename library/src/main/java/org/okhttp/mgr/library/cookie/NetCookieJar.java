package org.okhttp.mgr.library.cookie;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by huwentao on 16-4-19.
 */
public class NetCookieJar implements CookieJar {
    private PersistentCookieStore cookieStore = null;

    public NetCookieJar(PersistentCookieStore cookieStore) {
        if (cookieStore == null)
            throw new NullPointerException("PersistentCookieStore 不能为NULL");
        this.cookieStore = cookieStore;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookieStore != null) {
            if (cookies != null && cookies.size() > 0) {
                for (Cookie item : cookies) {
                    cookieStore.add(url, item);
                }
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return cookieStore.get(url);
    }
}
