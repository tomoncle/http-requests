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

package io.github.tomoncle.http.test;

import com.alibaba.fastjson.JSONObject;
import io.github.tomoncle.http.Requests;
import okhttp3.Headers;
import okhttp3.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author tomoncle
 */
public class TestRequests {

    @Test
    public void get() throws IOException {
        String request = Requests.GET.request("https://www.baidu.com");
        assert request != null;
    }

    @Test
    public void post() throws IOException {
        String url = "https://blog.csdn.net/king_aric/article/details/81023887";
        // set header
        Map<String, String> header = new HashMap<>();
        header.put("Cookies", "abc");
        // set body
        Requests.BodyMap bodyMap = new Requests.BodyMap();
        bodyMap.put("username", "tomoncle");
        // return json or text
        String request = Requests.POST.request(url, bodyMap, Headers.of(header));
        assert request != null;
        // return Response
        Response response = Requests.POST.response(url, bodyMap, Headers.of(header));
        assert response.code() == 200;
        response.close();
    }


    @Test
    public void untrustedCertificate() throws IOException {
        // 开启非受信证书
        Requests.enableSSL();
        // auto close
        try (Response response = Requests.GET.response("https://172.16.110.125:6443")) {
            assert response.code() != 200;
        }

        String request = Requests.GET.request("https://172.16.110.125:6443");
        JSONObject jsonObject = JSONObject.parseObject(request);
        assert Objects.equals(jsonObject.getString("reason"), "Unauthorized");
    }


}
