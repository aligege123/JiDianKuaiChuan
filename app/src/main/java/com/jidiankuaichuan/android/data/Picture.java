package com.jidiankuaichuan.android.data;

public class Picture {
    String name;

    String path;

    long size;

    public Picture(String name, String path, long size) {
        this.name = name;
        this.path = path;
        this.size = size = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
