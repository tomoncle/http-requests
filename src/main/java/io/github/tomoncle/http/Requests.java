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

import io.github.tomoncle.http.domain.DataType;
import io.github.tomoncle.http.domain.HttpMap;
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
    public static final MediaType FORM = parse("application/x-www-form-urlencoded");
    public static final MediaType OCTET_STREAM = parse("application/octet-stream");
    public static final MediaType MIXED = MediaType.parse("multipart/mixed");
    public static final MediaType ALTERNATIVE = MediaType.parse("multipart/alternative");
    public static final MediaType DIGEST = MediaType.parse("multipart/digest");
    public static final MediaType PARALLEL = MediaType.parse("multipart/parallel");
    public static final MediaType FORM_DATA = MediaType.parse("multipart/form-data");
    public static final Get GET = new Get();
    public static final Post POST = new Post();
    public static final Put PUT = new Put();
    public static final Patch PATCH = new Patch();
    public static final Delete DELETE = new Delete();
    public static final Head HEAD = new Head();
    private static final Logger logger = LoggerFactory.getLogger(Requests.class);
    private static final OkHttpClient client = initClient(false);
    private static final OkHttpClient sslClient = initClient(true);

    private Requests() {
    }

    private static OkHttpClient getClient(String url) {
        return Objects.requireNonNull(url).toLowerCase().startsWith("https") ? sslClient : client;
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

    private static OkHttpClient initClient(boolean ssl) {
        if (!ssl) {
            return new OkHttpClient().newBuilder().addInterceptor(new RequestsInterceptor()).build();
        }
        HostnameVerifier hostnameVerifier = (s, sslSession) -> true;
        TrustManager[] trustManagers = new TrustManager[]{x509TrustManager()};
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new SecureRandom());
            return new OkHttpClient().newBuilder().sslSocketFactory(sslContext.getSocketFactory(), x509TrustManager())//配置
                    .hostnameVerifier(hostnameVerifier)//配置
                    .addInterceptor(new RequestsInterceptor()).build();
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    private static Headers buildHeaders(Headers headers) {
        return null == headers ? Requests.HEADERS : headers;
    }


    private static Request.Builder requestBuilder(String url, Headers headers) {
        return new Request.Builder().url(url).headers(buildHeaders(headers));
    }

    private static FormBody formBody(Map<String, Object> map) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String name : map.keySet()) {
            builder.add(name, map.get(name).toString());
        }
        return builder.build();
    }

    private static RequestBody buildRequestBody(HttpMap map) {
        if (Objects.isNull(map)) {
            map = HttpMap.builder(DataType.BODY).build();
        }
        return (map.getDataType() == DataType.FORM) ? formBody(map.getValue()) : RequestBody.create(map.getValue().toJSONString(), JSON);
    }

    public static class Head extends AbstractTransfer {
        @Override
        Response method(String url, HttpMap map, Headers headers) throws IOException {
            return getClient(url).newCall(requestBuilder(url, headers).head().build()).execute();
        }
    }

    public static class Get extends AbstractBasic {
        @Override
        Response method(String url, HttpMap map, Headers headers) throws IOException {
            return getClient(url).newCall(requestBuilder(url, headers).build()).execute();
        }
    }

    public static class Post extends AbstractTransfer implements IExpandHandler, IUploadHandler {
        @Override
        Response method(String url, HttpMap map, Headers headers) throws IOException {
            return getClient(url).newCall(requestBuilder(url, headers).post(buildRequestBody(map)).build()).execute();
        }

        @Override
        public Response response(String url, byte[] bytes, Headers headers) throws IOException {
            return getClient(url).newCall(requestBuilder(url, headers).post(RequestBody.create(bytes, OCTET_STREAM)).build()).execute();
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
            return getClient(url).newCall(builder.build()).execute();
        }
    }

    public static class Put extends AbstractTransfer implements IExpandHandler {
        @Override
        Response method(String url, HttpMap map, Headers headers) throws IOException {
            return getClient(url).newCall(requestBuilder(url, headers).put(buildRequestBody(map)).build()).execute();
        }

        @Override
        public Response response(String url, byte[] bytes, Headers headers) throws IOException {
            return getClient(url).newCall(requestBuilder(url, headers).put(RequestBody.create(bytes, OCTET_STREAM)).build()).execute();
        }
    }

    public static class Patch extends AbstractTransfer {
        @Override
        Response method(String url, HttpMap map, Headers headers) throws IOException {
            return getClient(url).newCall(requestBuilder(url, headers).patch(buildRequestBody(map)).build()).execute();
        }
    }

    public static class Delete extends AbstractTransfer {
        @Override
        Response method(String url, HttpMap map, Headers headers) throws IOException {
            return getClient(url).newCall(requestBuilder(url, headers).delete(buildRequestBody(map)).build()).execute();
        }
    }

}