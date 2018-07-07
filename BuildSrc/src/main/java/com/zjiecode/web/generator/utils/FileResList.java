package com.zjiecode.web.generator.utils;

import com.zjiecode.web.generator.bean.FileResBean;

import java.util.ArrayList;
import java.util.List;

public class FileResList {
    private List<FileResBean> resList = new ArrayList<>();
    private List<FileResBean> javaList = new ArrayList<>();
    private static final FileResList instance = new FileResList();

    private FileResList() {
        //资源配置文件,目的文件地址，是以资源文件根目录为地址的
        resList.add(new FileResBean("/config/banner.txt", "banner.txt"));
        resList.add(new FileResBean("/config/application.properties", "application.properties"));
        resList.add(new FileResBean("/config/application-dev.properties", "application-dev.properties"));
        resList.add(new FileResBean("/config/application-prod.properties", "application-prod.properties"));
        //固定的java文件
        javaList.add(new FileResBean("/java/exception/AppException.java", "base/exception/AppException.java"));
        javaList.add(new FileResBean("/java/exception/BizException.java", "base/exception/BizException.java"));
        javaList.add(new FileResBean("/java/result/Result.java", "base/result/Result.java"));
        javaList.add(new FileResBean("/java/result/ResultCode.java", "base/result/ResultCode.java"));
        javaList.add(new FileResBean("/java/AppWebMvcConfigurer.java", "base/AppWebMvcConfigurer.java"));
        javaList.add(new FileResBean("/java/Pagination.java", "base/Pagination.java"));
        javaList.add(new FileResBean("/java/JavaWebApplication.java", "JavaWebApplication.java"));
    }

    public static FileResList getInstance() {
        return instance;
    }

    public List<FileResBean> getJavaList() {
        return javaList;
    }

    public List<FileResBean> getResList() {
        return resList;
    }
}
