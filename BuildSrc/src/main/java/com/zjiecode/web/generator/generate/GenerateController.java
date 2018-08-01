package com.zjiecode.web.generator.generate;

import com.squareup.javapoet.*;
import com.zjiecode.web.generator.bean.FieldBean;
import com.zjiecode.web.generator.utils.NameUtil;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 生成Controller文件代码
 */
public class GenerateController extends GenerateBase {
    private TypeName serviceClassName;
    private ClassName resultClassName;

    public GenerateController(ClassName dependClass, String table, List<FieldBean> fields, String basePackage) {
        super(dependClass, table, fields, basePackage,
                TypeSpec.classBuilder(NameUtil.className(table) + "Controller").addModifiers(Modifier.PUBLIC)
                        .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")).build())
                        .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestMapping"))
                                .addMember("value", "\"/$L\"", table).build())
                , "Controller");
        resultClassName = ClassName.bestGuess(basePackage + ".base.result.Result");
    }

    public void setServiceClassName(TypeName serviceClassName) {
        this.serviceClassName = serviceClassName;
    }

    //添加常见的增删改查的接口
    public GenerateController generate() {
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(serviceClassName, "service", Modifier.PRIVATE);
        fieldBuilder.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("javax.annotation.Resource")).build());
        classBuilder.addField(fieldBuilder.build());
        insertMethod();
        deleteMethod();
        updateMethod();
        findMethod();
        findAll();
        findByIdMethod();
        count();
        return this;
    }

    //生成返回的数据类型，套一层result
    private ParameterizedTypeName getReturnType(TypeName dataType) {
        return ParameterizedTypeName.get(resultClassName, dataType);
    }

    //生成插入数据的接口
    private void insertMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("insert");
        //参数验证的注解
        AnnotationSpec.Builder beanAnno = AnnotationSpec.builder(ClassName.bestGuess("javax.validation.Valid"));
        AnnotationSpec.Builder requestBodyAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestBody"));
        ParameterSpec.Builder beanParams = ParameterSpec.builder(beanClassName, table)
                .addAnnotation(beanAnno.build())
                .addAnnotation(requestBodyAnno.build());
        builder.addParameter(beanParams.build());
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(getReturnType(beanClassName));
        builder.addStatement("$L = service.insert($L)", table, table);
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.beginControlFlow("if ($L == null)", table);
        codeBuilder.addStatement("return $T.getBizFail(\"创建失败\")", resultClassName);
        codeBuilder.nextControlFlow("else");
        codeBuilder.addStatement("return $T.getSuccess($L)", resultClassName, table);
        codeBuilder.endControlFlow();
        builder.addCode(codeBuilder.build());
        //post路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping"));
        controllerAnno.addMember("value", "\"\"");
        builder.addAnnotation(controllerAnno.build());
        classBuilder.addMethod(builder.build());
    }

    //生成删除数据的接口
    private void deleteMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("delete");
        builder.returns(resultClassName);
        builder.addModifiers(Modifier.PUBLIC);
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.beginControlFlow("if (ids != null && !ids.isEmpty())");
        codeBuilder.addStatement("$T<$T> successIds = new $T<>()", List.class, Integer.class, ArrayList.class);
        codeBuilder.addStatement("boolean completeOne = false");
        codeBuilder.beginControlFlow("for ($T id : ids)", Integer.class);
        codeBuilder.addStatement("boolean delete = service.delete(id)");
        codeBuilder.beginControlFlow("if (delete)");
        codeBuilder.addStatement(" successIds.add(id)");
        codeBuilder.endControlFlow();
        codeBuilder.addStatement("completeOne = delete || completeOne");
        codeBuilder.endControlFlow();
        codeBuilder.beginControlFlow("if (!completeOne)");
        codeBuilder.addStatement("return $T.getBizFail(\"不存在或已经被删除\")", resultClassName);
        codeBuilder.nextControlFlow(" else");
        codeBuilder.addStatement("return new $T($T.SUCCESS, (successIds.size() == ids.size())?\"删除成功\":\"部分删除成功\", successIds.size())", resultClassName, ClassName.bestGuess(basePackage + ".base.result.ResultCode"));
        codeBuilder.endControlFlow();
        codeBuilder.endControlFlow();
        codeBuilder.addStatement("return $T.getBizFail(\"删除id不能为空\")", resultClassName);
        builder.addCode(codeBuilder.build());

        //delete路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.DeleteMapping"));
        controllerAnno.addMember("value", "\"\"");
        builder.addAnnotation(controllerAnno.build());
        //函数参数注解
        ParameterSpec.Builder idParams = ParameterSpec.builder(ParameterizedTypeName.get(List.class, Integer.class), "ids");
        AnnotationSpec.Builder requestBodyAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestBody"));
        idParams.addAnnotation(requestBodyAnno.build());
        builder.addParameter(idParams.build());
        classBuilder.addMethod(builder.build());
    }


    //生成修改的接口
    private void updateMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("update");
        builder.returns(getReturnType(ClassName.BOOLEAN.box()));
        builder.addModifiers(Modifier.PUBLIC);
        AtomicBoolean hasId = new AtomicBoolean(false);
        fields.stream().forEach(field -> {
            if ("id".equals(field.getName())) {
                hasId.set(true);
                return;
            }
        });
        builder.beginControlFlow("if ($L.getId() == null || $L.getId() == 0)", table, table);
        builder.addStatement("return $T.getBizFail(\"请提供需要更新的记录id\")", resultClassName);
        builder.nextControlFlow("else");
        builder.addStatement("boolean update = service.update($L)", table);
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.beginControlFlow("if (update)");
        codeBuilder.addStatement("return $T.getSuccess(true)", resultClassName);
        codeBuilder.nextControlFlow("else");
        codeBuilder.addStatement("return $T.getBizFail(\"更新失败或数据不存在\")", resultClassName);
        codeBuilder.endControlFlow();
        codeBuilder.endControlFlow();
        builder.addCode(codeBuilder.build());

        //put路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PutMapping"));
        controllerAnno.addMember("value", "\"\"");
        builder.addAnnotation(controllerAnno.build());
        //函数参数注解
        ParameterSpec.Builder idParams = ParameterSpec.builder(beanClassName, table);
        AnnotationSpec.Builder requestBodyAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestBody"));
        idParams.addAnnotation(requestBodyAnno.build());
        builder.addParameter(idParams.build());
        classBuilder.addMethod(builder.build());
    }

    //分页列表查询
    private void findMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("find");
        builder.returns(resultClassName);
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return $T.getSuccess(service.find($L, pageIndex, pageSize))", resultClassName, table);
        //get路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping"));
        controllerAnno.addMember("value", "\"\"");
        builder.addAnnotation(controllerAnno.build());
        //函数参数注解
        ParameterSpec.Builder beanParams = ParameterSpec.builder(beanClassName, table);
        ParameterSpec.Builder pageIndexParams = ParameterSpec.builder(Integer.class, "pageIndex");
        ParameterSpec.Builder pageSizeParams = ParameterSpec.builder(Integer.class, "pageSize");
        builder.addParameter(beanParams.build());
        builder.addParameter(pageIndexParams.build());
        builder.addParameter(pageSizeParams.build());
        classBuilder.addMethod(builder.build());
    }

    //根据id查询详情
    private void findByIdMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findById");
        builder.returns(getReturnType(beanClassName));
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return $T.getSuccess(service.findById(id))", resultClassName);
        //get路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping"));
        controllerAnno.addMember("value", "\"/detail\"");
        builder.addAnnotation(controllerAnno.build());
        //函数参数注解
        ParameterSpec.Builder idParams = ParameterSpec.builder(Integer.class, "id");
        builder.addParameter(idParams.build());
        classBuilder.addMethod(builder.build());
    }

    //查询全部
    private void findAll() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findAll");
        builder.returns(getReturnType(ParameterizedTypeName.get(ClassName.get(List.class), beanClassName)));
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return $T.getSuccess(service.findAll())", resultClassName);
        //get路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping"));
        controllerAnno.addMember("value", "\"/all\"");
        builder.addAnnotation(controllerAnno.build());
        classBuilder.addMethod(builder.build());
    }

    //查询存在的记录条数
    private void count() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("count");
        builder.returns(getReturnType(ClassName.INT.box()));
        builder.addModifiers(Modifier.PUBLIC);

        builder.addStatement("return $T.getSuccess(service.count())", resultClassName);
        //get路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping"));
        controllerAnno.addMember("value", "\"/count\"");
        builder.addAnnotation(controllerAnno.build());
        classBuilder.addMethod(builder.build());
    }
}
