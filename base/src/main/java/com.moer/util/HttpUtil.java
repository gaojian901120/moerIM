package com.moer.util;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gaoxuejian on 2016/5/31.
 * http 请求工具
 * 带有连接池
 */
public class HttpUtil {
    //默认连接超时时间
    final static int CONNECT_TIMEOUT = 3000;
    final static int REQUEST_TIMEOUT = 5000;
    final static int SOCKET_TIMEOUT = 3000;
    final static int MAX_RETRY = 1;
    public static HttpUtil httpUtil = new HttpUtil();
    private PoolingHttpClientConnectionManager cm = null;
    private CloseableHttpClient closeableHttpClient = null;

    private HttpUtil() {
        //连接池基本信息配置
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainsf)
                .register("https", sslsf)
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        // 将最大连接数增加到200
        cm.setMaxTotal(50000);
        // 将每个路由基础的连接增加到20
        cm.setDefaultMaxPerRoute(50000);
        //请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount > MAX_RETRY) {// 如果已经重试了5次，就放弃
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof SSLException) {// ssl握手异常
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
        closeableHttpClient = HttpClients.custom()
                .setConnectionManager(cm)
                //.setRetryHandler(httpRequestRetryHandler)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static String doGet(String url, Map<String, String> headers, Map<String, String> cookies, NameValuePair userPassword, int requestTimeout, int connentTimeout, int socketTimeout) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        return httpUtil.exec(httpGet, headers, cookies, userPassword, requestTimeout, connentTimeout, socketTimeout);
    }
    public static String doGet(String url, Map<String, String> maps) throws Exception{
        HttpGet httpGet = new HttpGet(url);
        if (null != maps && maps.size() > 0) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
                pairs.add(pair);
            }
            try {
                String param = EntityUtils.toString(new UrlEncodedFormEntity(pairs));
                httpGet.setURI(new URI(url + "?" + param));
            } catch (IOException ioe) {
                //LogUtil.error(ioe);
            } catch (URISyntaxException urie) {
                //LogUtil.error(urie);
            }
        }
        return httpUtil.exec(httpGet, null, null, null, REQUEST_TIMEOUT, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static String doGet(String url) throws Exception{
        HttpGet httpGet = new HttpGet(url);
        return httpUtil.exec(httpGet, null, null, null, REQUEST_TIMEOUT, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static String doPost(String url, String postBody, Map<String, String> headers, Map<String, String> cookies, NameValuePair userPassword) throws Exception{
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(postBody, "UTF-8");
        httpPost.setEntity(entity);
        return httpUtil.exec(httpPost, headers, cookies, userPassword, REQUEST_TIMEOUT, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    public static String doPost(String url, String postBody, int requestTimeout, int connectTimeout, int socketTimeout)throws Exception {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(postBody, "UTF-8");
        httpPost.setEntity(entity);
        return httpUtil.exec(httpPost, null, null, null, requestTimeout, connectTimeout, socketTimeout);
    }

    public static String doPost(String url, Map<String, String> maps)throws Exception{
        return doPost(url, maps, REQUEST_TIMEOUT, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    private static String doPost(String url, Map<String, String> maps, int requestTimeout, int connentTimeout, int socketTimeout) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        if (null != maps && maps.size() > 0) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
                pairs.add(pair);
            }
            try {
                HttpEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
                httpPost.setEntity(entity);
            } catch (IOException ioe) {
                //LogUtil.error(ioe);
            }
        }
        return httpUtil.exec(httpPost, null, null, null, requestTimeout, connentTimeout, socketTimeout);
    }

    public void setMaxPerRoute(String routeBase, int port, int num) {
        HttpHost httpHost = new HttpHost(routeBase, port);
        this.cm.setMaxPerRoute(new HttpRoute(httpHost), num);
    }

    private void defaultConfig(HttpRequestBase httpRequestBase, int requestTimeout, int connentTimeout, int socketTimeout) {
        httpRequestBase.setHeader("User-Agent", "JM Platform 1.0");
        httpRequestBase.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpRequestBase.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");//"en-US,en;q=0.5");
        httpRequestBase.setHeader("Accept-Charset", "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");

        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(requestTimeout)
                .setConnectTimeout(connentTimeout)
                .setSocketTimeout(socketTimeout)
                .build();
        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * 如果网站需要用户验证
     */
    private void setUserPassword(HttpClientContext context, String userName, String password) {
        if (context == null) {
            return;
        }
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        context.setCredentialsProvider(credsProvider);
    }

    /**
     * 设置请求自定义头部信息
     *
     * @param httpRequest
     * @param headers
     */
    private void setHeaders(HttpRequestBase httpRequest, Map<String, String> headers) {
        if (headers == null || headers.size() == 0) {
            return;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpRequest.setHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 设置请求的cookies
     *
     * @param httpRequest
     * @param cookies
     */
    private void setCookies(HttpRequestBase httpRequest, Map<String, String> cookies) {
        if (cookies == null || cookies.size() == 0) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
            sb.append("; ");
        }
        httpRequest.setHeader("Cookie", new String(sb));
    }

    private String exec(HttpRequestBase httprequest, Map<String, String> headers, Map<String, String> cookies, NameValuePair userPassword, int requestTimeout, int connentTimeout, int socketTimeout) throws Exception{
        CloseableHttpResponse response = null;
        String result = null;
        try {
            defaultConfig(httprequest, requestTimeout, connentTimeout, socketTimeout);
            HttpClientContext context = HttpClientContext.create();
            if (userPassword != null) {
                setUserPassword(context, userPassword.getName(), userPassword.getValue());
            }
            if (headers != null) {
                setHeaders(httprequest, headers);
            }
            if (cookies != null) {
                setCookies(httprequest, cookies);
            }
            response = closeableHttpClient.execute(httprequest, context);
            HttpEntity httpEntity = response.getEntity();
            result = EntityUtils.toString(httpEntity);
            response.close();
            return result;
        } catch (ConnectTimeoutException ioe) {
           throw ioe;
        } catch (SocketTimeoutException ste){
            throw ste;
        }
    }

}