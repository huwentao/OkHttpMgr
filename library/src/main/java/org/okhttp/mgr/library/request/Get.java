package org.okhttp.mgr.library.request;

import java.util.Map;
import java.util.Set;

/**
 * Created by huwentao on 16/10/8.
 */
public class Get extends HttpRequset {

    public Get(String requestUrl) {
        super(requestUrl);
    }

    public void request(){

    }

    /**
     * 构建URL请求参数
     *
     * @param url    url
     * @param params 参数列表
     * @return
     */
    private String buildUrlParams(String url, Map<String, String> params) {
        if (params != null && params.size() > 0) {
            StringBuilder requestUrl = new StringBuilder(url);
            Set<String> keySet = params.keySet();
            String[] keys = new String[keySet.size()];
            keySet.toArray(keys);
            requestUrl.append("?").append(keys[0]).append("=").append(params.get(keys[0]));
            for (int i = 1; i < keys.length; i++) {
                requestUrl.append("&").append(keys[i]).append("=").append(params.get(keys[i]));
            }
            url = requestUrl.toString();
        }
        return url;
    }
}
