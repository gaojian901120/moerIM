package com.moer;

import com.alibaba.fastjson.JSON;
import com.moer.entity.ImSession;
import com.moer.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/8/22.
 */
public class ReceiveMsgCient {
    static final int MAX_CLIENT = 100;
    public static void main(String [] args) {
        new ImClient().run();
    }
}

class ImClient extends Thread
{
    public static final String INIT_URL = "http://im.moer.cn:9000/init?uid=%s";
    public static final String CONNECT_URL = "http://%s/connect?uid=%s&token=%s&source=%s";
    public static final String PULL_URL = "http://%s/pull?uid=%s&sessionid=%s";
    @Override
    public void run() {
        Map<String,String> cookies = new HashMap<>();
        cookies.put("_jm_ppt_id","WPLNkDBlepHjltBs8qQDHThpqTnKzxhzU_XEhiW31CEUdrQCjBOQZEkhAqXpaDQYUk-uVw8k3x4zInSpacrFiA1389_7nLSTOqAhB2IKQ-bvlK3MLGKSEKMW");
        String result = HttpUtil.doGet(String.format(INIT_URL,"100809070"),null, cookies, null,0,0,0);
        Map<String, Object> resultMap = JSON.parseObject(result, Map.class);
        int code = Integer.valueOf(resultMap.get("code").toString());
        if (code != 1000) {
            System.out.println("call " + INIT_URL + " failed with message :" + resultMap.get("message"));
            return;
        }
        Map<String,String> data = (Map<String, String>) resultMap.get("data");
        String serverAddr = data.get("addr");
        String token = data.get("token");
        String uid = data.get("uid");
        result = HttpUtil.doGet(String.format(CONNECT_URL, serverAddr,uid, token, ImSession.SESSION_SOURCE_WEB));
        resultMap = JSON.parseObject(result, Map.class);
        if (resultMap == null) {
            System.out.println("call " + CONNECT_URL + " exception with message :" + result);
            return;
        }
        code = Integer.valueOf(resultMap.get("code").toString());
        if (code != 1000) {
            System.out.println("call " + CONNECT_URL + " failed with message :" + resultMap.get("message"));
            return;
        }
        data = (Map<String, String>) resultMap.get("data");
        String sessionId = data.get("sessionId");
        while(true) {
            try {
                result = HttpUtil.doGet(String.format(PULL_URL, serverAddr, uid, sessionId),null,null,null, 90000, 90000, 90000);
                resultMap = JSON.parseObject(result, Map.class);
                System.out.println(JSON.toJSONString(resultMap));
                Thread.sleep(1000);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

    }
}
