package com.moer;

import com.alibaba.fastjson.JSON;
import com.moer.common.Constant;
import com.moer.entity.ImSession;
import com.moer.util.HttpUtil;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/8/22.
 */
public class ReceiveMsgCient {
    static final int MAX_CLIENT = 100;
    public static void main(String [] args) throws Exception{
//        for (int i=1000000000;i<1000000001;i++) {
//            new ImClient(String.valueOf(i)).start();
//            try {
//                Thread.sleep(1);
//            }catch (Exception e){}
//        }
        System.out.print("Please input the uid:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int uid = Integer.valueOf(br.readLine());
        new ImClient(String.valueOf(uid)).start();
    }
}

class ImClient extends Thread
{
    public static final String INIT_URL = "http://im.moer.cn:9000/init?uid=%s";
    public static final String CONNECT_URL = "http://%s/connect?uid=%s&token=%s&source=%s";
    public static final String PULL_URL = "http://%s/pull?uid=%s&sessionid=%s&token=%s";
    private String uid;
    public ImClient(String uid){
        this.uid = uid;
    }
    @Override
    public void run() {
        while (true) {
            Map<String, String> cookies = new HashMap<>();
            cookies.put("_jm_ppt_id", "WPLNkDBlepHjltBs8qQDHThpqTnKzxhzU_XEhiW31CEUdrQCjBOQZEkhAqXpaDQYUk-uVw8k3x4zInSpacrFiA1389_7nLSTOqAhB2IKQ-bvlK3MLGKSEKMW");
            String initUrl = String.format(INIT_URL, this.uid);
            String result;
            try {
                result = HttpUtil.doGet(initUrl, null, cookies, null, 0, 0, 0);

            }catch (Exception e){
                return;
            }
            Map<String, Object> resultMap = JSON.parseObject(result, Map.class);
            int code = Integer.valueOf(resultMap.get("code").toString());
            //这种情况说明后端出错 应该重试  其他情况是前台错误 程序终止
            if (code != Constant.CODE_SUCCESS ) {
                System.out.println("call " + initUrl + " failed with message :" + resultMap.get("message"));
                if (code == Constant.CODE_NO_SERVER_NODE) {
                    try {
                        Thread.sleep(1000);
                        System.out.println(String.format("no server node sercice uid: ", uid));
                    }catch (Exception e){}
                    continue;
                }else {
                    System.out.println(String.format("client error with uid: ", uid));
                    return;
                }
            }
            Map<String, String> data = (Map<String, String>) resultMap.get("data");
            String serverAddr = data.get("addr");
            String token = data.get("token");
            String uid = data.get("uid");
            while (true) {
                String url = String.format(CONNECT_URL, serverAddr, uid, token, ImSession.SESSION_SOURCE_WEB);
                try {
                    result = HttpUtil.doGet(url);
                }catch (Exception e){
                    if(e instanceof  HttpHostConnectException || e instanceof ConnectTimeoutException){
                        break;
                    }
                    return;
                }

                if(result == null){
                    System.out.println("connect url" + url + " empty message ");
                    return;
                }
                resultMap = JSON.parseObject(result, Map.class);
                if (resultMap == null) {
                    System.out.println("call " + url + " exception with message :" + result);
                    return;
                }
                code = Integer.valueOf(resultMap.get("code").toString());
                if (code != Constant.CODE_SUCCESS) {
                    System.out.println("call " + url + " failed with message :" + resultMap.get("message"));
                    if (code == Constant.CODE_NODE_EXPIRED) {
                        break;
                    }else {
                        return;
                    }
                }else {
                    System.out.println("connect success " + uid + "!!!!!!!!!!");
                    data = (Map<String, String>) resultMap.get("data");
                    String sessionId = data.get("sessionId");
                    String token1 = data.get("token");
                    while (true) {
                        try {
                            String pullUrl = String.format(PULL_URL, serverAddr, uid, sessionId,token1);
                            result = HttpUtil.doGet(pullUrl, null, null, null, 2000, 2000, 20000);
                            if (result == null) {
                                System.out.println("no message received with uid:" + uid);
                                break;
                            } else {

                                resultMap = JSON.parseObject(result, Map.class);
                                code = Integer.valueOf(resultMap.get("code").toString());
                                if (code != Constant.CODE_SUCCESS) {
                                    System.out.println("call " + pullUrl + " failed with message :" + resultMap.get("message"));
                                    if (code == Constant.CODE_UNCONNECT || code == Constant.CODE_NODE_EXPIRED) {
                                        break;
                                    } else {
                                        return;
                                    }
                                }
                                System.out.println("recevied message: " + result);
                                try {
                                    Thread.sleep(1000);
                                }finally {

                                }
                            }
                        }catch(Exception e){
                            if (e instanceof ConnectTimeoutException || e instanceof HttpHostConnectException){//连接超时 说明节点存在问题
                                System.out.println("node maybe failed with uid："+ uid + "; refresh the nodelist");
                                break;
                            }else if(e instanceof  SocketTimeoutException){
                                System.out.println("no message received with uid:" + uid);
                                continue;
                            }else {
                                System.out.println(e.getMessage());

                            }
                        }
                    }
                }
            }
        }
    }
}
