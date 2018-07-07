package com.zjiecode.web.generator.generate;

import com.squareup.javapoet.*;
import com.zjiecode.web.generator.GenerateException;
import com.zjiecode.web.generator.bean.FieldBean;
import com.zjiecode.web.generator.utils.NameUtil;
import com.zjiecode.web.generator.utils.FieldTypeUtil;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 生成实体类
 */
public class GenerateBean extends GenerateBase {

    public GenerateBean(String table, List<FieldBean> fields, String basePackage) {
        super(table, fields, basePackage,
                TypeSpec.classBuilder(NameUtil.className(table)).addModifiers(Modifier.PUBLIC),
                "Bean");
    }

    public void generate() {
        if (fields == null || fields.isEmpty()) {
            throw new GenerateException(table + "表没有字段");
        }
        fields.stream().forEach(field -> {
            Object type = FieldTypeUtil.getType(field);
            FieldSpec.Builder fieldBuilder;
            String fieldName = NameUtil.fieldName(field.getName());
            if (type instanceof TypeName) {
                fieldBuilder = FieldSpec.builder((TypeName) type, fieldName, Modifier.PRIVATE);
            } else if (type instanceof Type) {
                fieldBuilder = FieldSpec.builder((Type) type, fieldName, Modifier.PRIVATE);
            } else {
                throw new GenerateException("不支持的数据类型");
            }

            //ID的注解
            if (field.isPrimary()) {
                fieldBuilder.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("javax.persistence.Id")).build());
            }
            //自动增长的注解
            if (field.isAutoIncrement()) {
                AnnotationSpec.Builder AotuIncAnnoBuilder = AnnotationSpec.builder(ClassName.bestGuess("javax.persistence.GeneratedValue"));
                AotuIncAnnoBuilder.addMember("strategy", "$T", ClassName.bestGuess("javax.persistence.GenerationType.IDENTITY"));
                fieldBuilder.addAnnotation(AotuIncAnnoBuilder.build());
            }
            //不能为空的注解
            if (field.isCanNull()) {
                AnnotationSpec.Builder notNullAnnoBuilder = AnnotationSpec.builder(ClassName.bestGuess("javax.validation.constraints.NotNull"));
                //如果有注释，就取注释，不然，就取字段名
                String name = field.isCommentEmpty() ? field.getName() : field.getComment();
                notNullAnnoBuilder.addMember("message", "\"$L不能为空\"", name);
                fieldBuilder.addAnnotation(notNullAnnoBuilder.build());
            }
            addField(fieldBuilder.build());
        });
    }

    /**
     * 添加一个字段
     * 自动添加对应的getter、setter
     */
    public GenerateBean addField(FieldSpec fieldSpec) {
        classBuilder.addField(fieldSpec);
        MethodSpec setter = MethodSpec.methodBuilder("set" + NameUtil.className(fieldSpec.name))
                .returns(TypeName.VOID)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.$L = $L", fieldSpec.name, fieldSpec.name)
                .addParameter(ParameterSpec.builder(fieldSpec.type, fieldSpec.name).build()).build();
        classBuilder.addMethod(setter);
        MethodSpec getter = MethodSpec.methodBuilder("get" + NameUtil.className(fieldSpec.name))
                .returns(fieldSpec.type)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return this.$L", fieldSpec.name).build();
        classBuilder.addMethod(getter);
        return this;
    }
}
