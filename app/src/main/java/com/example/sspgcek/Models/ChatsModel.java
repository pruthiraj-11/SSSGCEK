package com.example.sspgcek.Models;

public class ChatsModel {
    String msg;
    String MSG_TYPE;
    String sentTime;

    public ChatsModel() {
    }

    public ChatsModel(String msg, String MSG_TYPE, String sentTime) {
        this.msg = msg;
        this.MSG_TYPE=MSG_TYPE;
        this.sentTime=sentTime;
    }
    public String getSentTime() {
        return sentTime;
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }
    public String getMSG_TYPE() {
        return MSG_TYPE;
    }
    public void setMSG_TYPE(String MSG_TYPE) {
        this.MSG_TYPE = MSG_TYPE;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
