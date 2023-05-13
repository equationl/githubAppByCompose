/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.equationl.githubapp.common.net;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：<a href="https://github.com/jeasonlzy">https://github.com/jeasonlzy</a>
 * 版    本：1.0
 * 创建日期：2016/1/12
 * 描    述：OkHttp拦截器，主要用于打印日志
 * 修订历史：
 * ================================================
 */
public class HttpLoggingInterceptor implements Interceptor {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private volatile Level printLevel = Level.NONE;
    private java.util.logging.Level colorLevel;
    private final Logger logger;

    public enum Level {
        NONE,       //不打印log
        BASIC,      //只打印 请求首行 和 响应首行
        HEADERS,    //打印请求和响应的所有 Header
        BODY        //所有数据全部打印
    }

    public HttpLoggingInterceptor(String tag) {
        logger = Logger.getLogger(tag);
    }

    public void setPrintLevel(Level level) {
        if (printLevel == null) throw new NullPointerException("printLevel == null. Use Level.NONE instead.");
        printLevel = level;
    }

    public void setColorLevel(java.util.logging.Level level) {
        colorLevel = level;
    }

    private void log(String message) {
        logger.log(colorLevel, message);
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (printLevel == Level.NONE) {
            return chain.proceed(request);
        }

        //请求日志拦截
        logForRequest(request, chain.connection());

        //执行请求，计算请求时间
        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            addLogForRequest(request, "<-- HTTP FAILED: " + e);
            doLog(request);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        //响应日志拦截
        return logForResponse(request, response, tookMs);
    }

    private void logForRequest(Request request, Connection connection) throws IOException {
        boolean logBody = (printLevel == Level.BODY);
        boolean logHeaders = (printLevel == Level.BODY || printLevel == Level.HEADERS);
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;

        try {
            String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
            addLogForRequest(request, requestStartMessage);

            if (logHeaders) {
                if (hasRequestBody) {
                    // Request body headers are only present when installed as a network interceptor. Force
                    // them to be included (when available) so there values are known.
                    if (requestBody.contentType() != null) {
                        addLogForRequest(request,"\tContent-Type: " + requestBody.contentType());
                    }
                    if (requestBody.contentLength() != -1) {
                        addLogForRequest(request,"\tContent-Length: " + requestBody.contentLength());
                    }
                }
                Headers headers = request.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    String name = headers.name(i);
                    // Skip headers from the request body as they are explicitly logged above.
                    if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                        addLogForRequest(request,"\t" + name + ": " + headers.value(i));
                    }
                }

                addLogForRequest(request, "\theader: " + request.headers().toMultimap().toString());
                addLogForRequest(request," ");
                if (logBody && hasRequestBody) {
                    if (isPlaintext(requestBody.contentType())) {
                        bodyToString(request);
                    } else {
                        addLogForRequest(request,"\tbody: maybe [binary body], omitted!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            addLogForRequest(request,"--> END " + request.method());
        }
    }

    private Response logForResponse(Request request, Response response, long tookMs) {
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        ResponseBody responseBody = clone.body();
        boolean logBody = (printLevel == Level.BODY);
        boolean logHeaders = (printLevel == Level.BODY || printLevel == Level.HEADERS);

        try {
            addLogForResponse(request,"<-- " + clone.code() + ' ' + clone.message() + ' ' + request.url() + " (" + tookMs + "ms）");
            if (logHeaders) {
                Headers headers = clone.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    addLogForResponse(request,"\t" + headers.name(i) + ": " + headers.value(i));
                }
                addLogForResponse(request," ");
                if (logBody && HttpHeaders.hasBody(clone)) {
                    if (responseBody == null) return response;

                    if (isPlaintext(responseBody.contentType())) {
                        byte[] bytes = toByteArray(responseBody.byteStream());
                        MediaType contentType = responseBody.contentType();
                        String body = new String(bytes, getCharset(contentType));
                        addLogForResponse(request,"\tbody:" + body);
                        responseBody = ResponseBody.create(responseBody.contentType(), bytes);
                        return response.newBuilder().body(responseBody).build();
                    } else {
                        addLogForResponse(request,"\tbody: maybe [binary body], omitted!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            addLogForResponse(request,"<-- END HTTP");
            doLog(request);
        }
        return response;
    }

    //解析返回Code不是200时的消息
    private String parse(String jsonStr) {
        JSONObject json = null;
        String msg = "";
        try {
               json = new JSONObject(jsonStr);
               String code = json.getString("code");
                msg = json.getString("msg");
           } catch (JSONException e) {
               e.printStackTrace();
           }
           return  msg;
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    public static Charset getCharset(MediaType contentType) {
        Charset charset = contentType != null ? contentType.charset(UTF8) : UTF8;
        if (charset == null) charset = UTF8;
        return charset;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    public static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) return false;
        if (mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        subtype = subtype.toLowerCase();
        return subtype.contains("x-www-form-urlencoded") || subtype.contains("json") || subtype.contains("xml") || subtype.contains("html");
    }

    private void bodyToString(Request request) {
        try {
            Request copy = request.newBuilder().build();
            RequestBody body = copy.body();
            if (body == null) return;
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            Charset charset = getCharset(body.contentType());
            addLogForRequest(request, "\tbody_request:" + buffer.readString(charset));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final HashMap<Request, ArrayList<String>> logMap = new HashMap<>();

    private void addLogForRequest(Request url, String logStr) {
        ArrayList<String> logList = logMap.get(url);
        if (logList == null) {
            logList = new ArrayList<>();
        }
        logList.add(logStr);
        logMap.put(url, logList);
    }

    private void addLogForResponse(Request url, String logStr) {
        ArrayList<String> logList = logMap.get(url);
        if (logList != null) {
            logList.add(logStr);
            logMap.put(url, logList);
        }
    }

    synchronized private void doLog(Request url) {
        ArrayList<String> logList = logMap.get(url);
        if (logList != null) {
            log(" ");
            log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            for (String item: logList) {
                log("|\t" + item);
            }
            log("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            log(" ");
            logMap.remove(url);
        }
    }
}
