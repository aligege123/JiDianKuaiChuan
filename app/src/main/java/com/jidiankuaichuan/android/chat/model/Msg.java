package com.jidiankuaichuan.android.chat.model;

import org.litepal.crud.LitePalSupport;

public class Msg extends LitePalSupport {
    public static final int TYPE_RECEIVED = 0;

    public static final int TYPE_SEND = 1;

    private String address;

    private String content;

    private int type;

    public Msg(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
