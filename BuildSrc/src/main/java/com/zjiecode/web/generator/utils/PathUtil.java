package com.zjiecode.web.generator.utils;

import java.io.File;

public class PathUtil {
    //生成代码存放根目录
    public static File SRC_ROOT_DIR = new File("src/main/java/");
    public static File RES_ROOT_DIR = new File("src/main/resources/");

    //把包名变成路径
    public static String package2Path(String path) {
        return path.replace('.', '/');
    }
}
