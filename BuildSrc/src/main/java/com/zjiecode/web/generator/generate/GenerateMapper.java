package com.zjiecode.web.generator.generate;

import com.squareup.javapoet.*;
import com.zjiecode.web.generator.GenerateException;
import com.zjiecode.web.generator.bean.FieldBean;
import com.zjiecode.web.generator.utils.ClassNameUtil;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * 生成mapper文件代码
 */
public class GenerateMapper extends GenerateBase {
    private TypeName sqlProviderClassName;

    public GenerateMapper(ClassName dependClass, String table, List<FieldBean> fields, String basePackage) {
        super(dependClass, table, fields, basePackage,
                TypeSpec.interfaceBuilder(ClassNameUtil.className(table) + "Mapper").addModifiers(Modifier.PUBLIC)
                        .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.apache.ibatis.annotations.Mapper")).build())
                , "Mapper");
    }

    public void setSqlProviderClassName(TypeName sqlProviderClassName) {
        this.sqlProviderClassName = sqlProviderClassName;
    }

    //添加常见的增删改查的接口
    public GenerateMapper generate() {
        insertMethod();
        deleteMethod();
        updateMethod();
        findByIdMethod();
        findAll();
        findAllByPage();
        count();
        return this;
    }

    //生成插入数据的接口
    private void insertMethod() {
        MethodSpec.Builder insertBuilder = MethodSpec.methodBuilder("insert");
        insertBuilder.addParameter(beanClassName, table);
        //sql注解
        AnnotationSpec.Builder insertAnno = AnnotationSpec.builder(ClassName.bestGuess("org.apache.ibatis.annotations.Insert"));
        StringBuilder sbKey = new StringBuilder();
        StringBuilder sbValue = new StringBuilder();
        fields.stream().forEach(field -> {
            sbKey.append(field.getName()).append(",");
            sbValue.append("#{").append(field.getName()).append("}").append(",");
        });
        if (sbKey.length() <= 0) {
            throw new GenerateException("表[" + table + "]字段不能为空");
        }
        sbKey.deleteCharAt(sbKey.length() - 1);
        sbValue.deleteCharAt(sbValue.length() - 1);
        insertAnno.addMember("value", "\"INSERT INTO `$L`($L) VALUES($L)\"", table, sbKey.toString(), sbValue.toString());
        insertBuilder.addAnnotation(insertAnno.build());
        //获取插入的id的注解
        AnnotationSpec.Builder autoAnno = AnnotationSpec.builder(ClassName.bestGuess("org.apache.ibatis.annotations.Options"));
        autoAnno.addMember("useGeneratedKeys", "true");
        insertBuilder.addAnnotation(autoAnno.build());

        insertBuilder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        insertBuilder.returns(Boolean.class);
        classBuilder.addMethod(insertBuilder.build());
    }

    //生成删除数据的接口
    private void deleteMethod() {
        MethodSpec.Builder deleteBuilder = MethodSpec.methodBuilder("delete");
        deleteBuilder.addParameter(Integer.class, "id");
        deleteBuilder.returns(Boolean.class);
        AnnotationSpec.Builder deleteAnno = AnnotationSpec.builder(ClassName.bestGuess("org.apache.ibatis.annotations.Delete"));
        deleteAnno.addMember("value", "\"delete from `$L` where id=#{id}\"", table);
        deleteBuilder.addAnnotation(deleteAnno.build());
        deleteBuilder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        classBuilder.addMethod(deleteBuilder.build());
    }


    //生成修改的接口
    private void updateMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("update");
        builder.addParameter(beanClassName, table);
        builder.returns(Boolean.class);
        AnnotationSpec.Builder upateAnno = AnnotationSpec.builder(ClassName.bestGuess("org.apache.ibatis.annotations.UpdateProvider"));
        upateAnno.addMember("type", "$T.class", sqlProviderClassName);
        upateAnno.addMember("method", "$S", "update");
        builder.addAnnotation(upateAnno.build());
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        classBuilder.addMethod(builder.build());
    }

    //根据id查询记录
    private void findByIdMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findById");
        builder.addParameter(Integer.class, "id");
        builder.returns(beanClassName);
        AnnotationSpec.Builder anno = AnnotationSpec.builder(ClassName.bestGuess("org.apache.ibatis.annotations.Select"));
        anno.addMember("value", "\"SELECT * FROM `$L` WHERE id = #{id}\"", table);
        builder.addAnnotation(anno.build());
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        classBuilder.addMethod(builder.build());
    }

    //查询全部
    private void findAll() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findAll");
        builder.returns(ParameterizedTypeName.get(ClassName.get(List.class), beanClassName));
        AnnotationSpec.Builder anno = AnnotationSpec.builder(ClassName.bestGuess("org.apache.ibatis.annotations.Select"));
        anno.addMember("value", "\"SELECT * FROM `$L`\"", table);
        builder.addAnnotation(anno.build());
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        classBuilder.addMethod(builder.build());
    }

    //分页查询
    private void findAllByPage() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findAllByPage");
        builder.returns(ParameterizedTypeName.get(ClassName.get(List.class), beanClassName));
        AnnotationSpec.Builder anno = AnnotationSpec.builder(ClassName.bestGuess("org.apache.ibatis.annotations.Select"));
        anno.addMember("value", "\"SELECT * FROM `$L` limit #{param1},#{param2}\"", table);
        builder.addAnnotation(anno.build());
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        builder.addParameter(ParameterSpec.builder(Integer.class, "offset").build());
        builder.addParameter(ParameterSpec.builder(Integer.class, "length").build());
        classBuilder.addMethod(builder.build());
    }

    //查询存在的记录条数
    private void count() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("count");
        builder.returns(Integer.class);
        AnnotationSpec.Builder anno = AnnotationSpec.builder(ClassName.bestGuess("org.apache.ibatis.annotations.Select"));
        anno.addMember("value", "\"SELECT count(1) FROM `$L`\"", table);
        builder.addAnnotation(anno.build());
        builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        classBuilder.addMethod(builder.build());
    }
}
