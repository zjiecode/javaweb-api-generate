package com.zjiecode.web.generator.utils;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.zjiecode.web.generator.bean.FieldBean;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * mysql数据库字段类型到java的映射
 */
public class FieldTypeUtil {
    private static final Map<String, Object> typeMaps = new HashMap<>();

    static {
        typeMaps.put("bit", ArrayTypeName.of(Byte.class));
        typeMaps.put("tinyint", Byte.class);
        typeMaps.put("binary", ArrayTypeName.of(Byte.class));
        typeMaps.put("blob", ArrayTypeName.of(Byte.class));
        typeMaps.put("integer", Integer.class);
        typeMaps.put("int", Integer.class);
        typeMaps.put("bigint", Long.class);
        typeMaps.put("double", Double.class);
        typeMaps.put("float", Float.class);
        typeMaps.put("decimal", BigDecimal.class);
        typeMaps.put("date", Date.class);
        typeMaps.put("datetime", Date.class);
        typeMaps.put("text", String.class);
    }

    public static Object getType(FieldBean fieldBean) {
        if ("bit".equals(fieldBean.getType().toLowerCase()) && fieldBean.getLength() == 1) {
            return Boolean.class;
        } else {
            Object type = typeMaps.get(fieldBean.getType().toLowerCase());
            if (type == null) {
                type = String.class;
            }
            return type;
        }
    }
}
