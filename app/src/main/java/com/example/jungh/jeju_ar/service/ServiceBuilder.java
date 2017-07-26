package com.example.jungh.jeju_ar.service;

/**
 * Created by jungh on 2017-06-30.
 */

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ServiceBuilder {

    /** 캐시 50메가 **/
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024;

    private static Retrofit.Builder RETROFIT_BUILDER = new Retrofit.Builder();

    // No need to instantiate this class.
    private ServiceBuilder() {
    }

    public static <T> T createService(Class<T> serviceClass, String baseUrl) {
        OkHttpClient okHttpClient = getClient();

        RETROFIT_BUILDER.client(okHttpClient);
        RETROFIT_BUILDER.baseUrl(baseUrl);
        RETROFIT_BUILDER.addConverterFactory(JacksonConverterFactory.create());
        RETROFIT_BUILDER.addCallAdapterFactory(RxJavaCallAdapterFactory.create());

        Retrofit retrofit = RETROFIT_BUILDER.build();
        return retrofit.create(serviceClass);
    }

    private static OkHttpClient getClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();

        return client;
    }

    private static SSLSocketFactory createBadSslSocketFactory() {
        try {
            // Construct SSLSocketFactory that accepts any cert.
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager permissive = new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            context.init(null, new TrustManager[]{permissive}, null);
            return context.getSocketFactory();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
