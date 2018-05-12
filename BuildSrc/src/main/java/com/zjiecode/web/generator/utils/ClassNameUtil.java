package com.zjiecode.web.generator.utils;

public class ClassNameUtil {
    /**
     * 生成类名，也就是首字母大写
     *
     * @param name
     * @return
     */
    public static String className(String name) {
        if (name != null) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return name;
    }
}
