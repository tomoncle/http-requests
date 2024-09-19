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

package io.github.tomoncle.http.domain;

import com.alibaba.fastjson.JSONObject;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.Map;
import java.util.Objects;

/**
 * 项目地址：<a href="https://github.com/tomoncle/http-requests">项目地址</a>
 * <p>创建时间：2021-01-16
 * <p>描述信息：http请求参数
 *
 * @author tomoncle
 * @version 1.0.0
 * @apiNote 使用说明。
 * @since JDK1.8
 */
public class SimpleRequestBody {
    private final JSONObject value;
    private final DataType dataType;

    private SimpleRequestBody(JSONObject value, DataType dataType) {
        this.value = value;
        this.dataType = dataType;
    }

    public static Builder builder(DataType dataType) {
        return new Builder(dataType);
    }

    public static void main(String[] args) {
        SimpleRequestBody.builder(DataType.BODY).build();
    }

    public JSONObject getValue() {
        return value;
    }

    public DataType getDataType() {
        return dataType;
    }

    private FormBody formBody(Map<String, Object> map) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String name : map.keySet()) {
            builder.add(name, map.get(name).toString());
        }
        return builder.build();
    }

    private RequestBody buildRequestBody(SimpleRequestBody simpleRequestBody) {
        if (Objects.isNull(simpleRequestBody)) {
            simpleRequestBody = SimpleRequestBody.builder(DataType.BODY).build();
        }
        switch (simpleRequestBody.getDataType()) {
            case FORM:
                return formBody(simpleRequestBody.getValue());
            case BODY:
                return RequestBody.create(simpleRequestBody.getValue().toJSONString(), MediaType.parse("application/json; charset=utf-8"));
            default:
                return null;
        }
    }

    public RequestBody toRequestBody() {
        return buildRequestBody(this);
    }

    public static class Builder {
        private final JSONObject value = new JSONObject();
        private final DataType dataType;

        Builder(DataType dataType) {
            this.dataType = dataType;
        }

        public Builder setParam(String key, Object value) {
            this.value.put(key, value);
            return this;
        }

        public SimpleRequestBody build() {
            return new SimpleRequestBody(this.value, this.dataType);
        }
    }
}

