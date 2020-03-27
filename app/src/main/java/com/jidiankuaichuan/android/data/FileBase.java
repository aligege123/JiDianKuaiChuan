package com.jidiankuaichuan.android.data;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.LitePalSupport;

import java.lang.reflect.Field;

public class FileBase extends LitePalSupport {

    //文件序号
    private int id;
    //文件名
    private String name;
    //路径
    private String path;
    //类型
    private String type;
    //大小
    private long size;
    //进度
    private long progress = 0;
    //传送结果
    private int result = 0;
    //传输时间
    private String time;
    //操作类型
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static String toJsonStr(FileBase fileBase) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fileName", fileBase.getName());
            jsonObject.put("fileType", fileBase.getType());
            jsonObject.put("fileSize", fileBase.getSize());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    public static FileBase toFileBase(String jsonStr) {
        FileBase fileBase = new FileBase();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);

            String name = (String) jsonObject.get("fileName");
            String type = (String) jsonObject.get("fileType");
            long size = jsonObject.getLong("fileSize");

            fileBase.setName(name);
            fileBase.setType(type);
            fileBase.setSize(size);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fileBase;
    }
}
