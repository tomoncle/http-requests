package io.github.tomoncle.http;
/**
 * 项目地址：<a href="https://github.com/tomoncle/http-requests">项目地址</a>
 * <p>创建时间：2022-01-16
 * <p>描述信息：上传进度接口
 *
 * @author tomoncle
 * @version 1.0.0
 * @apiNote 使用说明。
 * @since JDK1.8
 */
public interface ProgressListener {
    void onProgressUpdate(long bytesWritten, long contentLength, boolean done);
}
