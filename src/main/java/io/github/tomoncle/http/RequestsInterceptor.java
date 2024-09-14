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
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 项目地址：<a href="https://github.com/tomoncle/http-requests">项目地址</a>
 * <p>创建时间：2021-01-16
 * <p>描述信息：实现自 {@link okhttp3.Interceptor} 接口，实现自定义请求拦截器
 *
 * @author tomoncle
 * @version 1.0.0
 * @apiNote 使用说明。
 * @since JDK1.8
 */
class RequestsInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(RequestsInterceptor.class);
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        String requestId = Thread.currentThread().getName();
        Request request = chain.request();
        long startTime = System.currentTimeMillis();
        this.logRequest(requestId, request);

        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.error("HTTP request failed: " + e);
            throw e;
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        this.logResponse(requestId, response, duration);
        return response;
    }

    private String format_value(Object obj) {
        if (Objects.isNull(obj)) {
            return "";
        }
        if (obj.toString().indexOf('\n') == -1) {
            return obj.toString();
        }
        return ("\n" + obj).replaceAll("\n+$", "");
    }

    private void logRequest(String requestId, Request request) {
        logger.debug("---> {} : Request  Method : {} {}", requestId, request.method(), request.url());
        logger.debug("---> {} : Request  Header : {}", requestId, this.format_value(request.headers()));
        logger.debug("---> {} : Request  Body   : {}", requestId, this.format_value(requestBodyToString(request)));
    }

    private void logResponse(String requestId, Response response, long duration) throws IOException {
        long contentLength = 0;
        if (Objects.nonNull(response.body()) && response.body().contentLength() > 0) {
            contentLength = response.body().contentLength();
        }
        try (ResponseBody responseBody = response.peekBody(contentLength)) {
            logger.debug("<--- {} : Response Code   : {} {} ({} ms, {} bytes)", requestId, response.code(), response.message(), duration, contentLength);
            logger.debug("<--- {} : Response Header : {}", requestId, this.format_value(response.headers()));
            logger.debug("<--- {} : Response Body   : {}", requestId, this.format_value(responseBodyToString(responseBody)));
        } catch (IOException e) {
            logger.error("解析响应值失败!" + e);
        }
    }

    private String requestBodyToString(Request request) {
        try {
            RequestBody body = request.body();
            if (Objects.isNull(body)) {
                return "";
            }
            MediaType mediaType = body.contentType();
            if (Objects.nonNull(mediaType) && (
                    mediaType.toString().startsWith("multipart/form-data; boundary=")
                            || mediaType.toString().startsWith("application/octet-stream")
            )) {
                return "Ignore Content-Type: " + body.contentType();
            }
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            return buffer.readString(UTF8);
        } catch (IOException e) {
            return "Failed to read request body";
        }
    }

    private String responseBodyToString(ResponseBody responseBody) {
        try {
            MediaType mediaType = responseBody.contentType();
            if (Objects.nonNull(mediaType) && (
                    mediaType.toString().startsWith("application/json")
                            || mediaType.toString().startsWith("application/xml")
                            || mediaType.toString().startsWith("text/")
            )) {
                return responseBody.string();
            }
            return "Ignore Content-Type: " + responseBody.contentType();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to read response body";
        }
    }
}