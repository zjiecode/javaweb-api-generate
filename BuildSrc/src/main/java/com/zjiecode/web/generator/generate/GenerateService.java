package com.zjiecode.web.generator.generate;

import com.squareup.javapoet.*;
import com.zjiecode.web.generator.bean.FieldBean;
import com.zjiecode.web.generator.utils.NameUtil;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * 生成Service文件代码
 */
public class GenerateService extends GenerateBase {
    private TypeName mapperClassName;

    public GenerateService(ClassName dependClass, String table, List<FieldBean> fields, String basePackage) {
        super(dependClass, table, fields, basePackage,
                TypeSpec.classBuilder(NameUtil.className(table) + "Service").addModifiers(Modifier.PUBLIC)
                        .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.stereotype.Service")).build())
                , "Service");
    }

    public void setMapperClassName(TypeName mapperClassName) {
        this.mapperClassName = mapperClassName;
    }

    //添加常见的增删改查的接口
    public GenerateService generate() {
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(mapperClassName, "mapper", Modifier.PRIVATE);
        fieldBuilder.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("javax.annotation.Resource")).build());
        classBuilder.addField(fieldBuilder.build());
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
        MethodSpec.Builder builder = MethodSpec.methodBuilder("insert");
        builder.addParameter(beanClassName, table);
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(beanClassName);
        builder.beginControlFlow("if (mapper.insert($L))", table);
        builder.addStatement("return $L", table);
        builder.nextControlFlow("else");
        builder.addStatement("return null");
        builder.endControlFlow();
        classBuilder.addMethod(builder.build());
    }

    //生成删除数据的接口
    private void deleteMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("delete");
        builder.addParameter(Integer.class, "id");
        builder.returns(Boolean.class);
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return mapper.delete(id)");
        classBuilder.addMethod(builder.build());
    }


    //生成修改的接口
    private void updateMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("update");
        builder.addParameter(beanClassName, table);
        builder.returns(Boolean.class);
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return mapper.update($L)", table);
        classBuilder.addMethod(builder.build());
    }

    //根据id查询记录详情
    private void findByIdMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findById");
        builder.addParameter(Integer.class, "id");
        builder.returns(beanClassName);
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return mapper.findById(id)");
        classBuilder.addMethod(builder.build());
    }

    //查询全部
    private void findAll() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findAll");
        builder.returns(ParameterizedTypeName.get(ClassName.get(List.class), beanClassName));
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return mapper.findAll()");
        classBuilder.addMethod(builder.build());
    }

    //分页查询
    private void findAllByPage() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("find");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(ParameterizedTypeName.get(ClassName.bestGuess(basePackage + ".base.Pagination"), ParameterizedTypeName.get(ClassName.get(List.class), beanClassName)));
        builder.addParameter(ParameterSpec.builder(beanClassName, table).build());
        builder.addParameter(ParameterSpec.builder(Integer.class, "pageIndex").build());
        builder.addParameter(ParameterSpec.builder(Integer.class, "pageSize").build());
        builder.beginControlFlow("if(pageIndex==null || pageIndex < 1)");
        builder.addStatement("pageIndex=1");
        builder.endControlFlow();

        builder.beginControlFlow("if(pageSize==null || pageSize < 1)");
        builder.addStatement("pageSize=20");
        builder.endControlFlow();

        builder.addStatement("$T<$T> records = mapper.find($L,(pageIndex - 1) * pageSize, pageSize)", List.class, beanClassName, table);
        builder.addStatement("return new $T(mapper.count(), pageIndex, records)", ClassName.bestGuess(basePackage + ".base.Pagination"));
        classBuilder.addMethod(builder.build());
    }


    //查询存在的记录条数
    private void count() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("count");
        builder.returns(Integer.class);
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return mapper.count()");
        classBuilder.addMethod(builder.build());
    }
}
