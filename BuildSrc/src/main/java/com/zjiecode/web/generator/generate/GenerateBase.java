package com.zjiecode.web.generator.generate;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.zjiecode.web.generator.GenerateException;
import com.zjiecode.web.generator.bean.FieldBean;
import com.zjiecode.web.generator.utils.PathUtil;

import java.io.File;
import java.util.List;

/**
 * 生成的基类
 */
public class GenerateBase {
    private static final String BIZ_PACKAGE_NAME = ".biz";
    protected TypeSpec.Builder classBuilder;
    protected ClassName beanClassName; //依赖一个生成的类
    protected String table;//表名
    protected String srcPackage;//生成文件的包名
    protected String basePackage;//基础的包名
    protected String type;//生成文件类型 Bean Mapper Controller等
    protected List<FieldBean> fields;//表的字段

    /**
     * 生成实体类，所以没有实体类类名
     */
    public GenerateBase(String table, List<FieldBean> fields, String basePackage, TypeSpec.Builder classBuilder, String type) {
        this.table = table;
        this.basePackage = basePackage;
        this.srcPackage = basePackage + BIZ_PACKAGE_NAME + "." + table;
        this.fields = fields;
        this.classBuilder = classBuilder;
        this.type = type;
    }

    /**
     * 生成初开实体类其他的类，都需要用到实体类的类名
     */
    public GenerateBase(ClassName beanClassName, String table, List<FieldBean> fields, String basePackage, TypeSpec.Builder classBuilder, String type) {
        this.beanClassName = beanClassName;
        this.table = table;
        this.basePackage = basePackage;
        this.srcPackage = basePackage + ".biz." + table;
        this.fields = fields;
        this.classBuilder = classBuilder;
        this.type = type;
    }

    //输出文件，并且返回完整的生成的类
    public ClassName out() {
        //判断文件夹不存在，就先生成项目文件夹
        File packageDir = new File(PathUtil.SRC_ROOT_DIR, PathUtil.package2Path(srcPackage));
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
        JavaFile javaFile = JavaFile.builder(srcPackage, classBuilder.build()).build();
        try {
            javaFile.writeTo(PathUtil.SRC_ROOT_DIR);
            String fullClass = javaFile.toJavaFileObject().getName()
                    .replace('/', '.')
                    .replace('\\', '.');
            if (fullClass.endsWith(".java")) {
                fullClass = fullClass.substring(0, fullClass.length() - 5);
            }
            System.out.println("[" + table + "]" + type + "生成成功:" + fullClass);
            return ClassName.bestGuess(fullClass);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GenerateException("[" + table + "]" + type + "生成失败:" + e.getMessage());
        }
    }
}
