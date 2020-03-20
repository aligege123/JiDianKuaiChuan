package com.jidiankuaichuan.android.data;

public class DeviceInfo {

    private String deviceName;

    private int imageId;

    private boolean selected = false;

    public DeviceInfo(String deviceName, int imageId){
        this.deviceName = deviceName;
        this.imageId = imageId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getImageId() {
        return imageId;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
