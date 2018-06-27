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
            String[] words = name.split("_");
            StringBuilder finalClassName = new StringBuilder();
            if (words.length > 0) {
                for (String s : words) {
                    finalClassName.append(s.substring(0, 1).toUpperCase() + s.substring(1));
                }
            }
            name = finalClassName.toString();
        }
        return name;
    }
}
