package com.moer.common;

import com.alibaba.fastjson.JSON;
import com.moer.entity.ImSession;
import com.moer.util.CryptUtil;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaoxuejian on 2018/6/5.
 */
public class ActionHandler {
    public String renderResult(int code, Object data) {
        return  renderResult(code,Constant.codeMap.get(code), data);
    }

    public String renderResult(int code, String message , Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        map.put("data", data);
        return JSON.toJSONString(map);
    }
    public String getLoginUid(String pptId) {
        try {
            pptId = pptId.replace("-","+").replace("_","/");
            String decode = CryptUtil.authcode(pptId,"293nAs9u23l&29", CryptUtil.DiscuzAuthcodeMode.Decode,0);
            if(decode.contains("_")){
                String [] decodeArr =  decode.split("_");
                return decodeArr[0];
            }
            return "";
        }catch (Exception e){
            return "";
        }
    }
    public String getLoginUid(HttpHeaders headers, String from){
        String uid = "";
        if(ImSession.FROM_WEB.equals(from)){
            String cookies = headers.get("Cookie");
            if(cookies != null && cookies.trim().length() > 0){
                String [] cookieArr = cookies.split(";");
                for (String cookie : cookieArr){
                    String [] item = cookie.split("=");
                    if(item[0].contains(ImSession.USER_COOKIE_FIELD)){
                        uid = getLoginUid(item[1]);
                        break;
                    }
                }
            }
        }else if(ImSession.FROM_ANDROID.equals(from) || ImSession.FROM_IOS.equals(from)) {
            String token = headers.get("token");
            uid = getLoginUid(token);
        }
        return uid;
    }
}
