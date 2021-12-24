package com.github.eros.domain;

public class Config extends BaseObject{
    /**
     * 所有的配置都是json格式的字符串
     */
    private String data;
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
