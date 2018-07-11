package com.zjiecode.web.generator.generate;

import com.squareup.javapoet.*;
import com.zjiecode.web.generator.bean.FieldBean;
import com.zjiecode.web.generator.utils.NameUtil;

import javax.lang.model.element.Modifier;
import java.util.List;
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
        findByIdMethod();
        findAll();
        findAllByPage();
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
        ParameterSpec.Builder beanParams = ParameterSpec.builder(beanClassName, table).addAnnotation(beanAnno.build());
        builder.addParameter(beanParams.build());
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(getReturnType(beanClassName));
        builder.addStatement("return $T.getSuccess(service.insert($L))", resultClassName, table);
        //post路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping"));
        controllerAnno.addMember("value", "\"/\"");
        builder.addAnnotation(controllerAnno.build());
        classBuilder.addMethod(builder.build());
    }

    //生成删除数据的接口
    private void deleteMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("delete");
        builder.returns(getReturnType(ClassName.BOOLEAN.box()));
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return $T.getSuccess(service.delete(id))", resultClassName);
        //delete路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.DeleteMapping"));
        controllerAnno.addMember("value", "\"/{id}\"");
        builder.addAnnotation(controllerAnno.build());
        //函数参数注解
        AnnotationSpec.Builder pathAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable"));
        pathAnno.addMember("value", "\"id\"");
        ParameterSpec.Builder idParams = ParameterSpec.builder(Integer.class, "id").addAnnotation(pathAnno.build());
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
        builder.addStatement("$L.setId(id)", table);
        builder.addStatement("return $T.getSuccess(service.update($L))", resultClassName, table);

        //put路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PutMapping"));
        controllerAnno.addMember("value", "\"/{id}\"");
        builder.addAnnotation(controllerAnno.build());
        //函数参数注解
        AnnotationSpec.Builder pathAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable"));
        pathAnno.addMember("value", "\"id\"");
        ParameterSpec.Builder idParams = ParameterSpec.builder(Integer.class, "id").addAnnotation(pathAnno.build());
        builder.addParameter(idParams.build());
        builder.addParameter(beanClassName, table);
        classBuilder.addMethod(builder.build());
    }

    //根据id查询记录
    private void findByIdMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findById");
        builder.returns(getReturnType(beanClassName));
        builder.addModifiers(Modifier.PUBLIC);
        builder.addStatement("return $T.getSuccess(service.findById(id))", resultClassName);
        //get路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping"));
        controllerAnno.addMember("value", "\"/{id}\"");
        builder.addAnnotation(controllerAnno.build());
        //函数参数注解
        AnnotationSpec.Builder pathAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable"));
        pathAnno.addMember("value", "\"id\"");
        ParameterSpec.Builder idParams = ParameterSpec.builder(Integer.class, "id").addAnnotation(pathAnno.build());
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

    //分页查询
    private void findAllByPage() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("findAllByPage");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(getReturnType(ParameterizedTypeName.get(ClassName.get(List.class), beanClassName)));
        //函数参数注解
        AnnotationSpec.Builder offsetAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable"));
        offsetAnno.addMember("value", "\"pageIndex\"");
        ParameterSpec.Builder idParams = ParameterSpec.builder(Integer.class, "pageIndex").addAnnotation(offsetAnno.build());
        builder.addParameter(idParams.build());
        //函数参数注解
        AnnotationSpec.Builder lengthAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable"));
        lengthAnno.addMember("value", "\"pageSize\"");
        ParameterSpec.Builder lengthParams = ParameterSpec.builder(Integer.class, "pageSize").addAnnotation(lengthAnno.build());
        builder.addParameter(lengthParams.build());
        builder.addStatement("return $T.getSuccess(service.findAllByPage(pageIndex,pageSize))", resultClassName);
        //get路由注解
        AnnotationSpec.Builder controllerAnno = AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping"));
        controllerAnno.addMember("value", "\"/{pageIndex}/{pageSize}\"");
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
