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

import io.github.tomoncle.http.domain.HttpMap;
import okhttp3.Headers;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * 项目地址：<a href="https://github.com/tomoncle/http-requests">项目地址</a>
 * <p>创建时间：2021-01-16
 * <p>描述信息：拓展的POST，PUT，DELETE，HEAD，PATCH 方法实现
 *
 * @author tomoncle
 * @version 1.0.0
 * @apiNote 使用说明。
 * @since JDK1.8
 */
abstract class AbstractTransfer extends AbstractBasic {
    public @Nullable String request(String url, HttpMap map) throws IOException {
        try (ResponseBody body = response(url, map).body()) {
            return body != null ? body.string() : null;
        }
    }

    public @Nullable String request(String url, HttpMap map, Headers headers) throws IOException {
        try (ResponseBody body = response(url, map, headers).body()) {
            return body != null ? body.string() : null;
        }
    }

    public Response response(String url, HttpMap map, Headers headers) throws IOException {
        return method(url, map, headers);
    }

    public Response response(String url, HttpMap map) throws IOException {
        return response(url, map, null);
    }


}