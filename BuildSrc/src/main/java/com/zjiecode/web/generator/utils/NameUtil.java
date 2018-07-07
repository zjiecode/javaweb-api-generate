package com.zjiecode.web.generator.utils;

public class NameUtil {
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

    /**
     * 生成字段名，驼峰命名法
     *
     * @param name
     * @return
     */
    public static String fieldName(String name) {
        if (name != null) {
            String[] words = name.split("_");
            StringBuilder fieldName = new StringBuilder();
            if (words.length > 0) {
                for (String s : words) {
                    if (fieldName.length() == 0) {
                        fieldName.append(s.substring(0, 1).toLowerCase() + s.substring(1));
                    } else {
                        fieldName.append(s.substring(0, 1).toUpperCase() + s.substring(1));
                    }
                }
            }
            name = fieldName.toString();
        }
        return name;
    }

}
