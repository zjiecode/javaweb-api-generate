package com.zjiecode.web.generator.bean;

/**
 * 管理固定和资源文件
 */
public class FileResBean {
    private String inUrl;
    private String outUrl;

    public FileResBean(String inUrl, String outUrl) {
        this.inUrl = inUrl;
        this.outUrl = outUrl;
    }

    public String getInUrl() {
        return inUrl;
    }

    public void setInUrl(String inUrl) {
        this.inUrl = inUrl;
    }

    public String getOutUrl() {
        return outUrl;
    }

    public void setOutUrl(String outUrl) {
        this.outUrl = outUrl;
    }
}
