package com.moer.config;

/**
 * Created by gaoxuejian on 2018/5/24.
 */
public class ImConfig
{
    private boolean multiEnd;

    private boolean multiWebEnd;

    private boolean multiAppEnd;

    public boolean isMultiEnd() {
        return multiEnd;
    }

    public void setMultiEnd(boolean multiEnd) {
        this.multiEnd = multiEnd;
    }

    public boolean isMultiWebEnd() {
        return multiWebEnd;
    }

    public void setMultiWebEnd(boolean multiWebEnd) {
        this.multiWebEnd = multiWebEnd;
    }

    public boolean isMultiAppEnd() {
        return multiAppEnd;
    }

    public void setMultiAppEnd(boolean multiAppEnd) {
        this.multiAppEnd = multiAppEnd;
    }
}
