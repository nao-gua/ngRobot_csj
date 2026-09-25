package com.csjbot.sdkDemo.service;

public class SocketEventVo {
    private String status;
    private String req;
    private String desc;
    private String type;

    public SocketEventVo(String req) {
        this.status = "0";
        this.req = req;
        this.desc = "success";
        this.type = "response";
    }

    public SocketEventVo() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReq() {
        return req;
    }

    public void setReq(String req) {
        this.req = req;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

