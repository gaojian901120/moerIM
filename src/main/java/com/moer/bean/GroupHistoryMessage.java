package com.moer.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class GroupHistoryMessage {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.mid
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    private String mid;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.send
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    private String send;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.recv
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    private String recv;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.msg_type
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    @JSONField(name="msg_type")
    private Integer msgType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.chat_type
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    @JSONField(name = "chat_type")
    private Integer chatType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.send_time
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    @JSONField(name = "send_time")
    private Long sendTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.show_type
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    @JSONField(name = "show_type")
    private Integer showType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.msg_seq
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    @JSONField(name = "msg_seq")
    private Integer msgSeq;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.mid
     *
     * @return the value of group_history_message.mid
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public String getMid() {
        return mid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.mid
     *
     * @param mid the value for group_history_message.mid
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setMid(String mid) {
        this.mid = mid == null ? null : mid.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.send
     *
     * @return the value of group_history_message.send
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public String getSend() {
        return send;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.send
     *
     * @param send the value for group_history_message.send
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setSend(String send) {
        this.send = send == null ? null : send.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.recv
     *
     * @return the value of group_history_message.recv
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public String getRecv() {
        return recv;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.recv
     *
     * @param recv the value for group_history_message.recv
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setRecv(String recv) {
        this.recv = recv == null ? null : recv.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.msg_type
     *
     * @return the value of group_history_message.msg_type
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public Integer getMsgType() {
        return msgType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.msg_type
     *
     * @param msgType the value for group_history_message.msg_type
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.chat_type
     *
     * @return the value of group_history_message.chat_type
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public Integer getChatType() {
        return chatType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.chat_type
     *
     * @param chatType the value for group_history_message.chat_type
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setChatType(Integer chatType) {
        this.chatType = chatType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.send_time
     *
     * @return the value of group_history_message.send_time
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public Long getSendTime() {
        return sendTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.send_time
     *
     * @param sendTime the value for group_history_message.send_time
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.show_type
     *
     * @return the value of group_history_message.show_type
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public Integer getShowType() {
        return showType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.show_type
     *
     * @param showType the value for group_history_message.show_type
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setShowType(Integer showType) {
        this.showType = showType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.msg_seq
     *
     * @return the value of group_history_message.msg_seq
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public Integer getMsgSeq() {
        return msgSeq;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.msg_seq
     *
     * @param msgSeq the value for group_history_message.msg_seq
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setMsgSeq(Integer msgSeq) {
        this.msgSeq = msgSeq;
    }
}