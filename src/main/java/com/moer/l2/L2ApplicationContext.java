package com.moer.l2;

import com.moer.config.ImConfig;
import com.moer.l2.context.L2GroupContext;
import com.moer.l2.context.L2UserContext;

/**
 * Created by gaoxuejian on 2018/5/19.
 * 服务层节点的全局上下文
 */
public class L2ApplicationContext {
    private static class L2ApplicationContextHolder {
        private static final L2ApplicationContext context = new L2ApplicationContext();
    }

    public final static L2ApplicationContext getInstance() {
        return L2ApplicationContextHolder.context;
    }

    private L2ApplicationContext() {
    }

    ;

    public L2UserContext userContext;
    public L2GroupContext groupContext;
    public ImConfig imConfig;
}
