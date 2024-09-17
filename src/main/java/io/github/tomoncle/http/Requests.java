/*
 * Copyright 2018 tomoncle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.tomoncle.http;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static okhttp3.MediaType.parse;

/**
 * 项目地址：<a href="https://github.com/tomoncle/http-requests">项目地址</a>
 * <p>创建时间：2021-01-16
 * <p>描述信息：基于 {@link okhttp3}，实现类似 <a href="https://github.com/psf/requests">
 * Python requests</a> 的一个简单而优雅的 HTTP 库
 *
 * @author tomoncle
 * @version 1.0.0
 * @apiNote 使用说明。
 * @see okhttp3.OkHttpClient
 * @since JDK1.8
 */
public final class Requests {
    public static final Headers HEADERS = new Headers.Builder().build();
    public static final MediaType JSON = parse("application/json; charset=utf-8");
    public static final MediaType FORM_DATA = MediaType.parse("multipart/form-data");
    public static final MediaType OCTET_STREAM = parse("application/octet-stream");
    public static final MediaType MIXED = MediaType.parse("multipart/mixed");
    public static final MediaType ALTERNATIVE = MediaType.parse("multipart/alternative");
    public static final MediaType DIGEST = MediaType.parse("multipart/digest");
    public static final MediaType PARALLEL = MediaType.parse("multipart/parallel");
    public static final MediaType FORM = parse("application/x-www-form-urlencoded");
    public static final Get GET = new Get();
    public static final Post POST = new Post();
    public static final Put PUT = new Put();
    public static final Patch PATCH = new Patch();
    public static final Delete DELETE = new Delete();
    public static final Head HEAD = new Head();
    private static final Logger logger = LoggerFactory.getLogger(Requests.class);
    private static OkHttpClient client = initClient();

    private Requests() {
    }

    public static void initClient(OkHttpClient okHttpClient) {
        assert Objects.nonNull(okHttpClient);
        client = okHttpClient;
    }

    private static X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        };
    }

    private static OkHttpClient initClient() {
        HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
        TrustManager[] trustManagers = new TrustManager[]{x509TrustManager()};
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                // 配置连接池
                .connectionPool(new ConnectionPool(10, 60L, TimeUnit.MINUTES))
                // .callTimeout(0, TimeUnit.SECONDS)         //完整请求超时时长，从发起到接收返回数据，默认值0，不限定
                .connectTimeout(60, TimeUnit.SECONDS)//与服务器建立连接的时长，默认10s
                .readTimeout(3600, TimeUnit.SECONDS) //读取服务器返回数据的时长
                .writeTimeout(3600, TimeUnit.SECONDS)//向服务器写入数据的时长，默认10s
                .retryOnConnectionFailure(true)  //失败重连
                .followRedirects(false)          //重定向
                .addInterceptor(new RequestsInterceptor());
        try {
            // 配置SSL证书
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory(), x509TrustManager()).hostnameVerifier(hostnameVerifier);
        } catch (GeneralSecurityException e) {
            logger.error("配置SSL证书失败!", e);
        }
        return builder.build();
    }

    /**
     * 验证 headers
     *
     * @param headers h
     * @return Headers
     */
    private static Headers buildHeaders(Headers headers) {
        return null == headers ? Requests.HEADERS : headers;
    }

    /**
     * create request builder
     *
     * @param url     request address
     * @param headers headers
     * @return Request.Builder
     */
    private static Request.Builder requestBuilder(String url, Headers headers) {
        return new Request.Builder().url(url).headers(buildHeaders(headers));
    }

    public static class Head extends AbstractBasic {
        @Override
        Response method(String url, RequestBody requestBody, Headers headers) throws IOException {
            return client.newCall(requestBuilder(url, headers).head().build()).execute();
        }
    }

    public static class Get extends AbstractBasic {
        @Override
        Response method(String url, RequestBody requestBody, Headers headers) throws IOException {
            return client.newCall(requestBuilder(url, headers).build()).execute();
        }
    }

    public static class Post extends AbstractTransfer implements IUploadHandler {
        @Override
        Response method(String url, RequestBody requestBody, Headers headers) throws IOException {
            return client.newCall(requestBuilder(url, headers).post(super.getDefaultRequestBody(requestBody)).build()).execute();
        }

        @Override
        public Response upload(String url, String filePath, Map<String, String> data, Map<String, String> header) throws IOException {
            return upload(url, filePath, null, data, header);
        }

        @Override
        public Response upload(String url, String filePath, String filename, Map<String, String> data, Map<String, String> header) throws IOException {
            File file = new File(filePath);
            if (Objects.isNull(filename)) {
                filename = file.getName();
            }
            MultipartBody.Builder formDataPart = new MultipartBody.Builder().setType(Objects.requireNonNull(FORM_DATA)).addFormDataPart("file", filename, RequestBody.create(file, FORM_DATA));
            if (Objects.nonNull(data)) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    formDataPart.addFormDataPart(entry.getKey(), entry.getValue());
                }
            }
            Request.Builder builder = new Request.Builder().url(url).post(formDataPart.build());
            if (Objects.nonNull(header)) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            return client.newCall(builder.build()).execute();
        }
    }

    public static class Put extends AbstractTransfer {
        @Override
        Response method(String url, RequestBody requestBody, Headers headers) throws IOException {
            return client.newCall(requestBuilder(url, headers).put(super.getDefaultRequestBody(requestBody)).build()).execute();
        }
    }

    public static class Patch extends AbstractTransfer {
        @Override
        Response method(String url, RequestBody requestBody, Headers headers) throws IOException {
            return client.newCall(requestBuilder(url, headers).patch(super.getDefaultRequestBody(requestBody)).build()).execute();
        }
    }

    public static class Delete extends AbstractTransfer {
        @Override
        Response method(String url, RequestBody requestBody, Headers headers) throws IOException {
            return client.newCall(requestBuilder(url, headers).delete(super.getDefaultRequestBody(requestBody)).build()).execute();
        }
    }
}