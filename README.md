# 简单的 Java http 工具类

# 安装

* 1.在项目pom.xml添加仓库

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>github Repositories</name>
        <url>https://tomoncle.github.io/maven/repository</url>
    </repository>
</repositories>
```

* 2.引入依赖

```xml
<dependency>
    <groupId>io.github.tomoncle</groupId>
    <artifactId>http-requests</artifactId>
    <version>1.0.0</version>
</dependency>
```

# 使用

* GET请求

```java
public class TestRequests {
    @SneakyThrows
    @Test
    public void get() {
        String request = Requests.GET.request("https://www.baidu.com");
        assert request != null;
    }
}
```

* POST请求

```java
public class TestRequests {
    @SneakyThrows
    @Test
    public void post() {
        String url = "https://api.tomoncle.com/post";
        // set header
        Map<String, String> header = new HashMap<>();
        header.put("Cookies", "abc");
        // set body
        HttpMap bodyMap = HttpMap.builder(DataType.FORM)
                .set("username", "tomoncle")
                .build();
        // return json or text
        String request = Requests.POST.request(url, bodyMap, Headers.of(header));
        assert request != null;
        // return Response
        Response response = Requests.POST.response(url, bodyMap, Headers.of(header));
        assert response.code() == 200;
        response.close();
    }

}
```

* 上传文件

```java
public class TestRequests {

    @Test
    @SneakyThrows
    public void testUpload() {
        Map<String, String> headers = new HashMap<>();
        headers.put("token", "123456");

        Map<String, String> data = new HashMap<>();
        data.put("user1", "tom");
        data.put("user2", "jack");

        Requests.POST.upload(
                "https://api.tomoncle.com/fileUpload",
                "/tmp/__diesel_schema_migrations.png",
                data,
                headers);
    }

}
```

* 对于Json处理

```java
public class TestRequests {
    @SneakyThrows
    @Test
    public void json() {
        String request = Requests.GET.request("https://api.tomoncle.com");
        JSONObject jsonObject = JSONObject.parseObject(request);
        assert Objects.equals(jsonObject.getString("code"), "200");
    }

}
```

* 支持：`GET`, `POST`, `HEAD`, `DELETE`, `PUT`, `PATCH`
