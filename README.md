# 简单的 Java http 工具类

# 安装
* 1.在项目pom.xml添加仓库
```xml 
 <repositories>
        <repository>
            <!--  tomoncle's private maven releases repository.  -->
            <id>tomoncle repository</id>
            <name>tomoncle Repositories</name>
            <url>https://github.com/tomoncle/m2/raw/master/repository/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
        <repository>
            <!--  snapshots repository.  -->
            <id>tomoncle snapshots</id>
            <name>tomoncle snapshots Repositories</name>
            <url>https://github.com/tomoncle/m2/raw/master/snapshots/</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
```

* 2.引入依赖
```xml
 <dependency>
    <groupId>tomoncle.github.io</groupId>
    <artifactId>http-requests</artifactId>
    <version>1.0.0</version>
 </dependency>
```

# 使用

* GET请求
```java
public class TestRequests {

    @Test
    public void get() throws IOException {
        String request = Requests.GET.request("https://www.baidu.com");
        assert request != null;
    }
}
```

* POST请求
```java
public class TestRequests {

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

}
```

* 对于非受信任的https资源
```java
public class TestRequests {

    @Test
    public void untrustedCertificate() throws IOException {
        // 开启非受信证书
        Requests.enableSSL();
        // auto close
        try (Response response = Requests.GET.response("https://172.16.110.125:6443")) {
            assert response.code() != 200;
        }
    }
    
 }
```

* 对于Json处理
```java
public class TestRequests {

    @Test
    public void json() throws IOException {
        Requests.enableSSL();
        String request = Requests.GET.request("https://172.16.110.125:6443");
        JSONObject jsonObject=JSONObject.parseObject(request);
        assert Objects.equals(jsonObject.getString("reason"), "Unauthorized");
    }
    
}
```

* 支持：`GET`, `POST`, `HEAD`, `DELETE`, `PUT`, `PATCH`

* 暂不支持：文件上传
