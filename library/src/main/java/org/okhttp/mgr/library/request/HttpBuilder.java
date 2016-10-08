package org.okhttp.mgr.library.request;

import android.content.Context;
import android.util.Log;

import org.okhttp.mgr.library.cookie.NetCookieJar;
import org.okhttp.mgr.library.cookie.PersistentCookieStore;
import org.okhttp.mgr.library.util.Const;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

/**
 * Created by huwentao on 16/10/8.
 */
public class HttpBuilder {
    private OkHttpClient.Builder builder;
    private long connectTimeout = 10000;
    private long readTimeout = 30000;
    private long writeTimeout = 60000;
    private long cacheMaxSize = 1024 * 1000 * 15;//默认缓存最大15M;
    private boolean isUseCache = false;
    private Context context;
    private OkHttpClient okHttpClient;
    private CookieJar cookieJar;
    private SSLParams sslParams;
    private HostnameVerifier hostnameVerifier;

    private HttpBuilder() {

    }

    private final static class InstanceHolder {
        public static HttpBuilder httpBuilder = new HttpBuilder();
    }

    public static HttpBuilder getInstance() {
        return InstanceHolder.httpBuilder;
    }

    public HttpBuilder init(Context context) {
        this.context = context;
        this.builder = new OkHttpClient.Builder();
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        this.builder.connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .hostnameVerifier(new DefaultHostnameVerifier());
        this.builder.cookieJar(new NetCookieJar(cookieStore));
        return this;
    }

    public HttpBuilder connectTimeout(long timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    public HttpBuilder readTimeout(long timeout) {
        this.readTimeout = timeout;
        return this;
    }

    public HttpBuilder writeTimeOut(long timeout) {
        this.writeTimeout = timeout;
        return this;
    }

    public HttpBuilder cache(boolean isUseCache, long cacheSize) {
        this.isUseCache = isUseCache;
        this.cacheMaxSize = cacheSize;
        return this;
    }

    /**
     * 全局cookie存取规则
     */
    public HttpBuilder setCookieStore(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
        return this;
    }

    public void build() {
        if (builder == null) {
            Log.e(Const.LOG_TAG, "请先调用 init() 方法初始化HttpConfig");
        }
        builder.connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .cookieJar(cookieJar);

        if (isUseCache) {
            final File baseDir = context.getCacheDir();
            if (baseDir != null && baseDir.exists()) {
                final File cacheDir = new File(baseDir, "HttpConfigCache");
                builder.cache(new Cache(cacheDir, cacheMaxSize));
            }
        }

        okHttpClient = builder.build();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public OkHttpClient getOkHttpsClient() {
        OkHttpClient.Builder builder = okHttpClient.newBuilder();
        builder.hostnameVerifier(hostnameVerifier);
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        return builder.build();
    }

    /**
     * 配置一个HTTPS的OkhttpClient
     *
     * @param hostnameVerifier https的全局访问规则
     * @param certificates     https的全局自签名证书
     * @return
     */
    public HttpBuilder httpsConfig(HostnameVerifier hostnameVerifier,
                                   InputStream... certificates) {
        if (okHttpClient == null) {
            Log.e(Const.LOG_TAG, "请在Application中初始化HttpConfig");
            return null;
        }
        this.hostnameVerifier = hostnameVerifier;
        this.sslParams = getSslSocketFactory(null, null, certificates);
        return this;
    }

    /**
     * https双向认证配置
     *
     * @param hostnameVerifier https的全局访问规则
     * @param bksFile          私钥证书
     * @param password         密钥
     * @param certificates     公钥证书
     * @return
     */
    public HttpBuilder httpsConfig(HostnameVerifier hostnameVerifier,
                                   InputStream bksFile, String password,
                                   InputStream... certificates) {
        if (okHttpClient == null) {
            Log.e(Const.LOG_TAG, "请在Application中初始化HttpConfig");
            return null;
        }
        this.hostnameVerifier = hostnameVerifier;
        this.sslParams = getSslSocketFactory(bksFile, password, certificates);
        return this;
    }

    /**
     * HTTPS相关参数
     */
    private class SSLParams {
        public SSLSocketFactory sSLSocketFactory;
        public X509TrustManager trustManager;
    }

    /**
     * 配置
     *
     * @param bksFile      私钥
     * @param password     私钥密钥
     * @param certificates 公钥证书
     * @return
     */
    private SSLParams getSslSocketFactory(InputStream bksFile, String password, InputStream[] certificates) {
        SSLParams sslParams = new SSLParams();
        try {
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager;
            if (trustManagers != null) {
                trustManager = new MyTrustManager(chooseTrustManager(trustManagers));
            } else {
                trustManager = new UnSafeTrustManager();
            }
            sslContext.init(keyManagers, new TrustManager[]{trustManager}, null);
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = trustManager;
            return sslParams;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (KeyManagementException e) {
            throw new AssertionError(e);
        } catch (KeyStoreException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * @param certificates 证书
     * @return
     */
    private TrustManager[] prepareTrustManager(InputStream... certificates) {
        if (certificates == null || certificates.length <= 0) return null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    if (certificate != null) certificate.close();
                } catch (IOException e) {
                    Log.e(Const.LOG_TAG, e.getMessage(), e);
                }
            }
            TrustManagerFactory trustManagerFactory;
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param bksFile
     * @param password
     * @return
     */
    private KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
        try {
            if (bksFile == null || password == null) return null;
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bksFile, password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param trustManagers
     * @return
     */
    private X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        return null;
    }

    /**
     * 忽略HTTPS证书校验
     */
    private class UnSafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    }

    /**
     *
     */
    private class MyTrustManager implements X509TrustManager {
        private X509TrustManager defaultTrustManager;
        private X509TrustManager localTrustManager;

        public MyTrustManager(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException {
            TrustManagerFactory managerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            managerFactory.init((KeyStore) null);
            defaultTrustManager = chooseTrustManager(managerFactory.getTrustManagers());
            this.localTrustManager = localTrustManager;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException ce) {
                localTrustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
