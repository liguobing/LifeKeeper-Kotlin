package com.lixyz.lifekeeperforkotlin.net;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.lixyz.lifekeeperforkotlin.net.https.HttpsSSLParams;
import com.lixyz.lifekeeperforkotlin.net.https.HttpsUtil;
import com.lixyz.lifekeeperforkotlin.utils.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.OkHttpClient;

@GlideModule
public class OKHttpGlideModule extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
//        try {
//            InputStream cerInputStream = context.getAssets().open(Constant.SERVER_CER);
//            InputStream bksInputStream = context.getAssets().open(Constant.CLIENT_CER);
//            HttpsSSLParams factory = HttpsUtil.Companion.getSslSocketFactory(new InputStream[]{cerInputStream}, bksInputStream, Constant.CLOUD_ADDRESS_CERTIFICATE_PASSWORD);
//            SSLSocketFactory sslSocketFactory = factory.getSSLSocketFactory();
//            X509TrustManager trustManager = factory.getTrustManager();
//            if (sslSocketFactory != null && trustManager != null) {
//                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(60 * 1000, TimeUnit.MILLISECONDS)
//                        .readTimeout(5 * 60 * 1000, TimeUnit.MILLISECONDS)
//                        .writeTimeout(5 * 60 * 1000, TimeUnit.MILLISECONDS)
//                        .addInterceptor(new ProgressInterceptor())
//                        .sslSocketFactory(sslSocketFactory, trustManager)
//                        .hostnameVerifier((hostname, session) -> Constant.HOST_NAME.equals(hostname))
//                        .build();
//                registry.prepend(GlideUrl.class,
//                        InputStream.class, new OkHttpUrlLoader.Factory((Call.Factory)client)
//                );
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
        builder.setBitmapPool(new LruBitmapPool(0));
    }
}