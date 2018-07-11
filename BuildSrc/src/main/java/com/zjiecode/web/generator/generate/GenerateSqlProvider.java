package com.zjiecode.web.generator.generate;

import com.squareup.javapoet.*;
import com.zjiecode.web.generator.bean.FieldBean;
import com.zjiecode.web.generator.utils.NameUtil;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * 生成mapper文件代码
 */
public class GenerateSqlProvider extends GenerateBase {
    public GenerateSqlProvider(ClassName dependClass, String table, List<FieldBean> fields, String basePackage) {
        super(dependClass, table, fields, basePackage,
                TypeSpec.classBuilder(NameUtil.className(table) + "SqlProvider").addModifiers(Modifier.PUBLIC),
                "SqlProvider");
    }

    //添加常见的增删改查的接口
    public GenerateSqlProvider generate() {
        MethodSpec.Builder updateBuilder = MethodSpec.methodBuilder("update");
        updateBuilder.addParameter(beanClassName, table);
        updateBuilder.addException(Exception.class);
        updateBuilder.returns(String.class);
        updateBuilder.addStatement("$T sb = new $T()", StringBuilder.class, StringBuilder.class);
        fields.stream().forEach(field -> {
            updateBuilder.beginControlFlow("if (null!=$L.get$L())", table, NameUtil.className(field.getName()))
                    .addStatement("sb.append(\",$L = #{$L}\")", field.getName(), NameUtil.fieldName(field.getName()))
                    .endControlFlow();
        });
        ClassName BizException = ClassName.bestGuess(basePackage + ".base.exception.BizException");
        updateBuilder.beginControlFlow("if (sb.length() == 0)")
                .addStatement(" throw new $T(\"没有需要更新的字段\")", BizException)
                .endControlFlow();
        fields.stream().forEach(field -> {
            if ("id".equalsIgnoreCase(field.getName())) {
                updateBuilder.beginControlFlow("if ($L.getId() == null ||\"\".equals($L.getId()))", table, table)
                        .addStatement(" throw new $T(\"请提供更新的数据主键\")", BizException)
                        .endControlFlow();
            }
        });
        updateBuilder.addStatement("sb.append(\" where id=#{id}\")");
        updateBuilder.addStatement("sb.deleteCharAt(0)");
        updateBuilder.addStatement("sb.insert(0, \"update user set \")");
        updateBuilder.addStatement("return sb.toString()");
        updateBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addMethod(updateBuilder.build());
        return this;
    }
}
