package com.github.eros.client.retrofit.entry;

import com.github.eros.common.model.BaseObject;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 15:06
 */
public class ConfigInfo extends BaseObject {
    private String configData;
    private Long lastModified;
    private String md5;

    public String getConfigData() {
        return configData;
    }

    public void setConfigData(String configData) {
        this.configData = configData;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
