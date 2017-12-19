package com.hp.choosephoto.model;

/**
 * Created by zrg on 2017/7/5.
 */
public class ImageInfo {
    //eg:/storage/emulated/0/DCIM/Camera/20170705_152314.jpg
    private String name;//照片名字:20170705_152314.jpg
    private String path;//照片路径：/storage/emulated/0/DCIM/Camera/20170705_152314.jpg
    private long lastModified;//照片最后修改时间，排序用

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

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
