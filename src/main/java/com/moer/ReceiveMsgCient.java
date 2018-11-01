package com.moer;

import com.alibaba.fastjson.JSON;
import com.moer.common.Constant;
import com.moer.entity.ImSession;
import com.moer.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by gaoxuejian on 2018/8/22.
 */
public class ReceiveMsgCient {
    static final int MAX_CLIENT = 100;
    public static void main(String [] args) {
        StringBuffer sb = new StringBuffer(6);
        Random r = new Random();
        for (int i=0;i<6;i++){
            sb.append((char)(r.nextInt(62) + 49));
        }
        for (int i=100000000;i<100001000;i++) {
            new ImClient(String.valueOf(i)).start();
            try {
                Thread.sleep(1000);
            }catch (Exception e){}
        }
    }
}

class ImClient extends Thread
{
    public static final String INIT_URL = "http://im.moer.cn:9000/init?uid=%s";
    public static final String CONNECT_URL = "http://%s/connect?uid=%s&token=%s&source=%s";
    public static final String PULL_URL = "http://%s/pull?uid=%s&sessionid=%s";
    private String uid;
    public ImClient(String uid){
        this.uid = uid;
    }
    @Override
    public void run() {
        while (true) {
            Map<String, String> cookies = new HashMap<>();
            cookies.put("_jm_ppt_id", "WPLNkDBlepHjltBs8qQDHThpqTnKzxhzU_XEhiW31CEUdrQCjBOQZEkhAqXpaDQYUk-uVw8k3x4zInSpacrFiA1389_7nLSTOqAhB2IKQ-bvlK3MLGKSEKMW");
            String result = HttpUtil.doGet(String.format(INIT_URL, this.uid), null, cookies, null, 0, 0, 0);
            Map<String, Object> resultMap = JSON.parseObject(result, Map.class);
            int code = Integer.valueOf(resultMap.get("code").toString());
            //这种情况说明后端出错 应该重试  其他情况是前台错误 程序终止
            if (code != Constant.CODE_SUCCESS ) {
                System.out.println("call " + INIT_URL + " failed with message :" + resultMap.get("message"));
                if (code == Constant.CODE_NO_SERVER_NODE) {
                    try {
                        Thread.sleep(1000);
                    }catch (Exception e){}
                    continue;
                }else {
                    return;
                }
            }
            Map<String, String> data = (Map<String, String>) resultMap.get("data");
            String serverAddr = data.get("addr");
            String token = data.get("token");
            String uid = data.get("uid");
            while (true) {
                result = HttpUtil.doGet(String.format(CONNECT_URL, serverAddr, uid, token, ImSession.SESSION_SOURCE_WEB));
                resultMap = JSON.parseObject(result, Map.class);
                if (resultMap == null) {
                    System.out.println("call " + CONNECT_URL + " exception with message :" + result);
                    return;
                }
                System.out.println("connect success " + uid + "!!!!!!!!!!");
                code = Integer.valueOf(resultMap.get("code").toString());
                if (code != Constant.CODE_SUCCESS) {
                    System.out.println("call " + CONNECT_URL + " failed with message :" + resultMap.get("message"));
                    if (code == Constant.CODE_NODE_EXPIRED) {
                        break;
                    }else {
                        return;
                    }

                }else {
                    data = (Map<String, String>) resultMap.get("data");
                    String sessionId = data.get("sessionId");
                    while (true) {
                        try {
                            result = HttpUtil.doGet(String.format(PULL_URL, serverAddr, uid, sessionId), null, null, null, 90000, 90000, 90000);
                            resultMap = JSON.parseObject(result, Map.class);
                            code = Integer.valueOf(resultMap.get("code").toString());
                            if (code != Constant.CODE_SUCCESS) {
                                System.out.println("call " + CONNECT_URL + " failed with message :" + resultMap.get("message"));
                                if (code == Constant.CODE_UNCONNECT) {
                                    break;
                                }else {
                                    return;
                                }
                            }
                            System.out.println(JSON.toJSONString(resultMap));
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }

        }
    }
}
