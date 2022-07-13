package com.alice1530.mcloud.client;

import com.alice1530.mcloud.config.MCloudProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.alice1530.mcloud.config.Cookie;
import com.alice1530.mcloud.model.CaiyunResponse;
import com.alice1530.mcloud.util.EncryptUtil;
import com.alice1530.mcloud.util.JsonUtil;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

//import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MCloudClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCloudClient.class);
    private OkHttpClient okHttpClient;
    private MCloudProperties MCloudProperties;
    private static int BUF_SIZE = 65536;

    public MCloudClient(MCloudProperties MCloudProperties) {

        String token = MCloudProperties.getToken();
        String account = MCloudProperties.getAccount();
        String encrypt = MCloudProperties.getEncrypt();
        String tel = MCloudProperties.getTel();

        if (!StringUtils.hasLength(token)) {
            LOGGER.error("token为空");
        } else if (!StringUtils.hasLength(account)) {
            LOGGER.error("account为空");
        } else if (!StringUtils.hasLength(encrypt)) {
            LOGGER.error("encrypt为空");
        } else if (!StringUtils.hasLength(tel)) {
            LOGGER.error("tel为空");
        } else {
            LOGGER.info("\ntoken:   {},\naccount: {}, \nencrypt: {},\ntel:     {}", token, account, encrypt, tel);
        }

//        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append("ORCHES-C-TOKEN=").append(token).append(";")
//                .append("ORCHES-C-ACCOUNT=").append(account).append(";")
//                .append("ORCHES-I-ACCOUNT-ENCRYPT=").append(encrypt).append(";");

        Cookie.setToken(token);
        Cookie.setAccount(account);
        Cookie.setEncrypt(encrypt);
        Cookie.setTel(tel);

        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateNowStr = sdf.format(new Date());
                String key = EncryptUtil.getRandomSring(16);
                Request request = chain.request();
                String sign = "";
                if (request.url().url().getHost().equals("yun.139.com")) {
                    sign = EncryptUtil.getNewSign(bodyToString(request), dateNowStr, key);
                    request = request.newBuilder()
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", MCloudProperties.getAgent())
                            .addHeader("Accept", "application/json, text/plain, */*")
                            .addHeader("x-huawei-channelSrc", "10000034")
                            .addHeader("x-inner-ntwk", "2")
                            .addHeader("mcloud-channel", "1000101")
                            .addHeader("mcloud-client", "10701")
                            .addHeader("mcloud-sign", dateNowStr + "," + key + "," + sign)
                            .addHeader("content-type", "application/json;charset=UTF-8")
                            .addHeader("caller", "web")
                            .addHeader("CMS-DEVICE", "default")
                            .addHeader("x-DeviceInfo", "||9|85.0.4183.83|chrome|85.0.4183.83|||windows 10||zh-CN|||")
                            .addHeader("x-SvcType", "1")
                            .addHeader("referer", "https://yun.139.com/w/")
                            .addHeader("Cookie", Cookie.getCookie())
                            .build();
                }
                return chain.proceed(request);
            }
        })
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)
                .build();
        this.okHttpClient = okHttpClient;
        this.MCloudProperties = MCloudProperties;
    }

    private static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (Exception e) {
            LOGGER.error("{} in error, {}", request.url().url().getHost(), e);
            return "";
        }
    }

    private void login() {
        // todo 暂不支持登录功能
    }

    public Response download(String url) throws Exception {
        Request.Builder builder = new Request.Builder().header("referer", "https://yun.139.com/w/");
        Request request = builder.url(url).build();
        try {
            return okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new Exception(e);
        }

    }
/*
    public Response download(String url, HttpServletRequest httpServletRequest, long size) throws Exception {
        Request.Builder builder = new Request.Builder().header("referer", "https://yun.139.com/w/");
        String range = httpServletRequest.getHeader("range");
        boolean isMac = false;
        String userAgent = httpServletRequest.getHeader("User-Agent").toLowerCase();
        if (userAgent.contains("darwin") && userAgent.contains("webdavfs")) {
            isMac = true;
        }
        if (range != null) {
            // 如果range最后 >= size， 则去掉
            String[] split = range.split("-");
            if (split.length == 2) {
                String end = split[1];
                if (Long.parseLong(end) >= size) {
                    range = (range.substring(0, range.lastIndexOf('-') + 1) + size);
                }
                builder.header("range", range);
            } else if (!isMac) {
                builder.header("range", range);
            }
        }

        String ifRange = httpServletRequest.getHeader("if-range");
        if (ifRange != null) {
            builder.header("if-range", ifRange);
        }


        Request request = builder.url(url).build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            return response;
        } catch (IOException e) {
            throw new Exception(e);
        }
    }
*/
    public void upload(String url, byte[] bytes, final int offset, final int byteCount, String taskId, long totalSize, long point, String fileName) throws Exception {
        Request request = new Request.Builder()
                .addHeader("uploadtaskID", taskId)
                .addHeader("contentSize", String.valueOf(totalSize))
                .addHeader("Range", "bytes=" + point + "-" + (point + byteCount - 1))
                .addHeader("Content-Type", "text/plain")
                .addHeader("Content-Length", String.valueOf(byteCount))
//                .addHeader("Content-Type","text/plain;name="+ UrlEncodeUtil.encodeURIComponent(fileName))
                .post(RequestBody.create(MediaType.parse(""), bytes, offset, byteCount))
                .url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            LOGGER.info("upload: {}, code: {}", url, response.code());
            if (!response.isSuccessful()) {
                LOGGER.error("请求失败，url={}, code={}, resp={}", url, response.code(), response.body().string());
                throw new Exception("请求失败：" + url);
            }
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    public String post(String url, Object body) throws Exception {
        String bodyAsJson = JsonUtil.toJson(body);
        Request request = new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyAsJson))
                .url(getTotalUrl(url)).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            String res = toString(response.body());
            LOGGER.info("post: {}, code: {}, body: {}", url, response.code(), bodyAsJson);
            CaiyunResponse<Object> resObject = JsonUtil.readValue(res, new TypeReference<CaiyunResponse<Object>>() {
            });
            if (!response.isSuccessful() || !resObject.getSuccess()) {
                LOGGER.error("请求失败，url={}, code={}, resp={}", url, response.code(), res);
                throw new Exception("请求失败：" + url);
            }
            return res;
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    public String put(String url, Object body) throws Exception {
        Request request = new Request.Builder()
                .put(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JsonUtil.toJson(body)))
                .url(getTotalUrl(url)).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            LOGGER.info("put: {}, code: {}", url, response.code());
            if (!response.isSuccessful()) {
                LOGGER.error("请求失败，url={}, code={}, resp={}", url, response.code(), response.body().string());
                throw new Exception("请求失败：" + url);
            }
            return toString(response.body());
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    public String get(String url, Map<String, String> params) throws Exception {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(getTotalUrl(url)).newBuilder();
            params.forEach(urlBuilder::addQueryParameter);

            Request request = new Request.Builder().get().url(urlBuilder.build()).build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                LOGGER.info("get {}, code {}", urlBuilder.build(), response.code());
                if (!response.isSuccessful()) {
                    throw new Exception("请求失败：" + urlBuilder.build().toString());
                }
                return toString(response.body());
            }

        } catch (Exception e) {
            throw new Exception(e);
        }

    }

    private String toString(ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            return null;
        }
        return responseBody.string();
    }

    private String getTotalUrl(String url) {
        if (url.startsWith("http")) {
            return url;
        }
        return MCloudProperties.getUrl() + url;
    }
}
