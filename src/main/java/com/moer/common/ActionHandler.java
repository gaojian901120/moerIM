package com.moer.common;

import com.alibaba.fastjson.JSON;

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
}
