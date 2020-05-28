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

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static okhttp3.MediaType.parse;

/**
 * 一个简单的类似 python requests 的http 工具类
 * @author tomoncle
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class Requests {

    public static final Requests DEFAULT;
    private static final Headers headers;
    private static final MediaType JSON;
    private static final MediaType FORM;
    private static final MediaType OCTET_STREAM;
    public static final Requests.Get GET;
    public static final Requests.Post POST;
    public static final Requests.Put PUT;
    public static final Requests.Patch PATCH;
    public static final Requests.Delete DELETE;
    public static final Requests.Head HEAD;
    private static final X509TrustManager X_509_TRUST_MANAGER;
    private static final HostnameVerifier HOSTNAME_VERIFIER;
    private static final TrustManager[] TRUST_MANAGERS;
    private static OkHttpClient client;

    static {
        client = getClient(false);
        JSON = parse("application/json; charset=utf-8");
        FORM = parse("application/x-www-form-urlencoded");
        OCTET_STREAM = parse("application/octet-stream");
        X_509_TRUST_MANAGER = x509TrustManager();
        HOSTNAME_VERIFIER = (s, sslSession) -> true;
        TRUST_MANAGERS = new TrustManager[]{X_509_TRUST_MANAGER};
        DEFAULT = new Requests();
        GET = DEFAULT.new Get();
        POST = DEFAULT.new Post();
        PUT = DEFAULT.new Put();
        PATCH = DEFAULT.new Patch();
        DELETE = DEFAULT.new Delete();
        HEAD = DEFAULT.new Head();
        headers = Headers.of(new HashMap<String, String>() {{
            put("Accept", "*/*");
        }});
    }

    private Requests() {
    }

    /**
     * Requests.enableSSL();
     * String request = Requests.GET.request("https://172.16.110.125:6443")
     */
    public static void enableSSL() {
        client = getClient(true);
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

    private static OkHttpClient getClient(boolean ssl) {
        if (!ssl) {
            return new OkHttpClient();
        }
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, TRUST_MANAGERS, new SecureRandom());
            return new OkHttpClient().newBuilder()
                    .sslSocketFactory(sslContext.getSocketFactory(), X_509_TRUST_MANAGER)//配置
                    .hostnameVerifier(HOSTNAME_VERIFIER)//配置
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验证 headers
     *
     * @param headers h
     * @return Headers
     */
    private static Headers buildHeaders(Headers headers) {
        return null == headers ? Requests.headers : headers;
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

    /**
     * 解析map为 form string
     *
     * @param map Map
     * @return string
     */
    private static String buildContent(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            try {
                String str;
                sb.append(URLEncoder.encode((entry.getKey().toString()), "UTF-8")).append("=");
                if (entry.getValue().getClass().isArray()) {
                    StringBuilder s = new StringBuilder();
                    for (Object o : (Object[]) entry.getValue()) {
                        if (s.length() > 0) s.append(",");
                        s.append(o);
                    }
                    str = s.toString();
                } else if (Objects.equals(entry.getValue().getClass().getSimpleName(), "") ||
                        Objects.equals(entry.getValue().getClass().getSimpleName(), "ArrayList")) {
                    // entry.getValue().getClass().getSimpleName()取出的List类型为空
                    str = entry.getValue().toString().trim();
                    str = str.substring(1, str.length() - 1);
                } else {
                    str = entry.getValue().toString();
                }
                sb.append(URLEncoder.encode((str), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public enum RequestDataType {
        BODY, FORM, STREAM
    }


    /**
     * 流式操作接口
     */
    private interface U {
        Response response(String url, byte[] bytes, Headers headers) throws IOException;
    }

    // 参数父类
    private static class HttpMap extends JSONObject {
    }

    // body 传递参数
    public static class BodyMap extends Requests.HttpMap {
    }

    // form 传递参数
    public static class FormMap extends Requests.HttpMap {
    }

    /**
     * 不用在body或form中传递参数的抽象方法
     */
    private abstract class G {
        public
        @Nullable
        String request(String url) throws IOException {
            try (ResponseBody responseBody = response(url).body()) {
                return null == responseBody ? null : responseBody.string();
            }
        }

        public
        @Nullable
        String request(String url, Headers headers) throws IOException {
            try (ResponseBody responseBody = response(url, headers).body()) {
                return null == responseBody ? null : responseBody.string();
            }
        }

        public Response response(String url) throws IOException {
            return response(url, null);
        }

        public Response response(String url, Headers headers) throws IOException {
            return method(url, null, headers, null);
        }

        /**
         * 实现该方法执行对应的请求
         *
         * @throws IOException 需要捕获
         */
        abstract Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException;
    }


    /**
     * 验证 RequestBody
     *
     * @param map data
     * @return RequestBody
     */
    private static RequestBody buildRequestBody(HttpMap map, RequestDataType bodyType) {
        return (bodyType == RequestDataType.FORM) ?
                FormBody.create(FORM, buildContent(map, bodyType)) :
                RequestBody.create(JSON, buildContent(map, bodyType));
    }

    /**
     * 解析map
     *
     * @param map  map
     * @param type type
     * @return String
     */
    private static String buildContent(HttpMap map, RequestDataType type) {
        if (map == null) map = new HttpMap();
        return (RequestDataType.FORM == type) ? buildContent(map) : map.toJSONString();
    }

    /**
     * 需要在body或form传递参数的
     */
    private abstract class P extends G {

        public
        @Nullable
        String request(String url, FormMap map) throws IOException {
            try (ResponseBody body = response(url, map).body()) {
                return body != null ? body.string() : null;
            }
        }

        public
        @Nullable
        String request(String url, BodyMap map) throws IOException {
            try (ResponseBody body = response(url, map).body()) {
                return body != null ? body.string() : null;
            }
        }

        public
        @Nullable
        String request(String url, FormMap map, Headers headers) throws IOException {
            try (ResponseBody body = response(url, map, headers).body()) {
                return body != null ? body.string() : null;
            }
        }

        public
        @Nullable
        String request(String url, BodyMap map, Headers headers) throws IOException {
            try (ResponseBody body = response(url, map, headers).body()) {
                return body != null ? body.string() : null;
            }
        }

        public Response response(String url, FormMap map, Headers headers) throws IOException {
            return method(url, map, headers, RequestDataType.FORM);
        }

        public Response response(String url, BodyMap map, Headers headers) throws IOException {
            return method(url, map, headers, RequestDataType.BODY);
        }

        public Response response(String url, FormMap map) throws IOException {
            return response(url, map, null);
        }

        public Response response(String url, BodyMap map) throws IOException {
            return response(url, map, null);
        }

    }

    public class Head extends P {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).head().build()).execute();
        }
    }

    public class Get extends G {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).build()).execute();
        }
    }

    public class Post extends P implements U {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).post(buildRequestBody(map, dataType)).build()).execute();
        }

        @Override
        public Response response(String url, byte[] bytes, Headers headers) throws IOException {
            return client.newCall(requestBuilder(url, headers).post(RequestBody.create(OCTET_STREAM, bytes)).build()).execute();
        }

    }

    public class Put extends P {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).put(buildRequestBody(map, dataType)).build()).execute();
        }
    }

    public class Patch extends P {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).patch(buildRequestBody(map, dataType)).build()).execute();
        }
    }

    public class Delete extends P {
        @Override
        Response method(String url, HttpMap map, Headers headers, RequestDataType dataType) throws IOException {
            return client.newCall(requestBuilder(url, headers).delete(buildRequestBody(map, dataType)).build()).execute();
        }
    }

}