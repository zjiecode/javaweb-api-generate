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
    public void generate() {
        generateUpdate();
        generateFind();
    }

    private void generateUpdate() {
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
        updateBuilder.addStatement("sb.insert(0, \"update $L set \")",table);
        updateBuilder.addStatement("return sb.toString()");
        updateBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addMethod(updateBuilder.build());
    }

    private void generateFind() {
        MethodSpec.Builder findBuilder = MethodSpec.methodBuilder("find");
        findBuilder.addModifiers(Modifier.PUBLIC);
        findBuilder.addParameter(beanClassName, table);
        findBuilder.addParameter(Integer.class, "pageIndex");
        findBuilder.addParameter(Integer.class, "pageSize");
        findBuilder.addException(Exception.class);
        findBuilder.returns(String.class);
        findBuilder.addStatement("$T sb = new $T()", StringBuilder.class, StringBuilder.class);
        fields.stream().forEach(field -> {
            findBuilder.beginControlFlow("if (null!=$L.get$L())", table, NameUtil.className(field.getName()))
                    .addStatement("sb.append(\" and $L = #{arg0.$L}\")", field.getName(),NameUtil.fieldName(field.getName()))
                    .endControlFlow();
        });
        findBuilder.beginControlFlow("if (sb.length() != 0)");
        findBuilder.addStatement("sb.delete(0, 5)");
        findBuilder.addStatement("sb.insert(0, \" where \")");
        findBuilder.endControlFlow();

        findBuilder.beginControlFlow("if (pageIndex == null || pageIndex == 0)");
        findBuilder.addStatement("pageIndex = 1");
        findBuilder.endControlFlow();

        findBuilder.beginControlFlow("if (pageSize == null || pageSize == 0)");
        findBuilder.addStatement("pageSize = 20");
        findBuilder.endControlFlow();

        findBuilder.addStatement("sb.append(\" limit \")");
        findBuilder.addStatement("sb.append((pageIndex - 1) * pageSize)");
        findBuilder.addStatement("sb.append(\",\")");
        findBuilder.addStatement("sb.append(pageSize)");


        findBuilder.addStatement("$T fields = new $T()", StringBuilder.class, StringBuilder.class);
        findBuilder.addStatement("fields.append(\"SELECT \")");

        for (int i = 0; i < fields.size(); i++) {
            FieldBean field = fields.get(i);
            findBuilder.addStatement("fields.append(\" `$L` as $L" + ((i < fields.size() - 1) ? "," : "") + "\")", field.getName(), NameUtil.fieldName(field.getName()));
        }
        findBuilder.addStatement("fields.append(\" from $L\")", table);
        findBuilder.addStatement("String sql = fields.toString() + sb.toString()");
        findBuilder.addStatement("return sql");


        classBuilder.addMethod(findBuilder.build());
    }
}
