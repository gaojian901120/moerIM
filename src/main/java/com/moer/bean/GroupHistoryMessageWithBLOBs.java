package com.moer.bean;

public class GroupHistoryMessageWithBLOBs extends GroupHistoryMessage {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.msg
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    private String msg;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column group_history_message.extp
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    private String extp;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.msg
     *
     * @return the value of group_history_message.msg
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public String getMsg() {
        return msg;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.msg
     *
     * @param msg the value for group_history_message.msg
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setMsg(String msg) {
        this.msg = msg == null ? null : msg.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column group_history_message.extp
     *
     * @return the value of group_history_message.extp
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public String getExtp() {
        return extp;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column group_history_message.extp
     *
     * @param extp the value for group_history_message.extp
     *
     * @mbggenerated Mon Jun 11 14:24:09 CST 2018
     */
    public void setExtp(String extp) {
        this.extp = extp == null ? null : extp.trim();
    }
}