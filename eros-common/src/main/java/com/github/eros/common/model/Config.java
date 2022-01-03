package com.github.eros.common.model;

public class Config extends BaseObject{
    /**
     * 所有的配置都是json格式的字符串
     */
    private String data;
    private String app;
    private String group;
    private String namespace;
    private Long lastModified;
    private String checkMd5;

    public Config() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public String getCheckMd5() {
        return checkMd5;
    }

    public void setCheckMd5(String checkMd5) {
        this.checkMd5 = checkMd5;
    }
}
