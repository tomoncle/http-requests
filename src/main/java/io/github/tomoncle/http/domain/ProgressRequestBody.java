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

import io.github.tomoncle.http.ProgressListener;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * 项目地址：<a href="https://github.com/tomoncle/http-requests">项目地址</a>
 * <p>创建时间：2022-01-16
 * <p>描述信息：上传实体
 *
 * @author tomoncle
 * @version 1.0.0
 * @apiNote 使用说明。
 * @since JDK1.8
 */
public class ProgressRequestBody extends RequestBody {
    private static final Logger logger = LoggerFactory.getLogger(ProgressRequestBody.class);
    private final RequestBody requestBody;
    private final ProgressListener listener;
    private final String logPrompt;

    public ProgressRequestBody(RequestBody requestBody, ProgressListener listener, String logPrompt) {
        this.requestBody = requestBody;
        this.listener = listener;
        this.logPrompt = getDefaultValue(logPrompt);
    }

    public ProgressRequestBody(RequestBody requestBody, String logPrompt) {
        this.requestBody = requestBody;
        this.logPrompt = getDefaultValue(logPrompt);
        this.listener = new ProgressListener() {
            private long lastUpdateTime = System.currentTimeMillis();
            private long lastBytesWritten = 0L;

            @Override
            public void onProgressUpdate(long bytesWritten, long contentLength, boolean done, String logPrompt) {
                float percentage = Float.parseFloat(String.valueOf(bytesWritten)) / contentLength;
                DecimalFormat df = new DecimalFormat("0.00%");
                String progress = df.format(percentage);
                long currentTime = System.currentTimeMillis();
                long timeElapsed = currentTime - lastUpdateTime;
                if (timeElapsed > 1000 || done) {
                    long bytesUploaded = bytesWritten - lastBytesWritten;
                    DecimalFormat dfs = new DecimalFormat("0.00 MB/s");
                    float speedPerSec = (float) (bytesUploaded / 1024.0 / 1024.0 / (timeElapsed / 1000.0));
                    String speed = dfs.format(speedPerSec);
                    lastBytesWritten = bytesWritten;
                    lastUpdateTime = currentTime;
                    logger.debug("{}上传进度: {}, 当前速度: {}", logPrompt, progress, speed);
                }
                if (done) {
                    logger.debug("{}上传完成!", logPrompt);
                }
            }
        };
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    private String getDefaultValue(String val) {
        return val != null && !val.isEmpty() ? val : "";
    }

    @Override
    public long contentLength() {
        try {
            return requestBody.contentLength();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        BufferedSink bufferedSink = Okio.buffer(new ForwardingSink(sink) {
            long bytesWritten = 0L;

            @Override
            public void write(@NotNull Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                bytesWritten += byteCount;
                listener.onProgressUpdate(bytesWritten, contentLength(), bytesWritten == contentLength(), logPrompt);
            }
        });
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }
}

