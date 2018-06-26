package com.zjiecode.web.generator;

import com.squareup.javapoet.ClassName;
import com.zjiecode.web.generator.bean.FieldBean;
import com.zjiecode.web.generator.bean.FileResBean;
import com.zjiecode.web.generator.generate.*;
import com.zjiecode.web.generator.utils.FileResList;
import com.zjiecode.web.generator.utils.PathUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GenerateTask extends DefaultTask {
    @Input
    private ConfigExtension config;
    private Connection con;

    public ConfigExtension getConfig() {
        return config;
    }

    public void setConfig(ConfigExtension config) {
        this.config = config;
    }

    @TaskAction
    private void action() {
        try {
            Class.forName(config.getDbDriver());
            String url = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8&useSSL=false", config.getDbHost(), config.getDbPort(), config.getDbName());
            con = DriverManager.getConnection(url, config.getDbUser(), config.getDbPassword());
            if (con.isClosed()) {
                throw new GenerateException("连接数据库失败， 请检查连接");
            } else {
                System.out.println("连接数据库成功");
                getTable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GenerateException("连接数据库失败， 请检查连接");
        }
    }

    //查询数据库存在的表
    private void getTable() throws SQLException {
        String sql = "select table_name from information_schema.tables where table_schema='"+config.getDbName()+"'";
        PreparedStatement ps = con.prepareStatement(sql);
        //3.ResultSet类，用来存放获取的结果集！！
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String table = rs.getString("table_name");
            getTableDetail(table);
        }
    }

    //获取表结构
    private void getTableDetail(String table) throws SQLException {
        System.out.println("正在处理表：" + table);
        String sql = "select  column_name as col, column_comment as comment,IS_NULLABLE as canNull,COLUMN_KEY,EXTRA,DATA_TYPE as type,NUMERIC_PRECISION as length from information_schema.columns where  table_schema ='" + config.getDbName() + "'  and table_name = '" + table + "' ORDER  by ORDINAL_POSITION ASC";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        List<FieldBean> fields = new ArrayList<>();
        while (rs.next()) {
            String col = rs.getString("col");
            String comment = rs.getString("comment");
            String canNull = rs.getString("canNull");
            String columnKey = rs.getString("COLUMN_KEY");
            String extra = rs.getString("EXTRA");
            String type = rs.getString("type");
            int length = rs.getInt("length");
            fields.add(new FieldBean(col, comment, type, length, "NO".equalsIgnoreCase(canNull), "PRI".equalsIgnoreCase(columnKey), "auto_increment".equalsIgnoreCase(extra)));
        }
        generate(table, fields, config.getBasePackage());
    }

    //生成代码文件
    private void generate(String table, List<FieldBean> fields, String srcPackage) {
        //判断文件夹不存在，就先生成项目文件夹
        File packageDir = new File(PathUtil.SRC_ROOT_DIR, PathUtil.package2Path(config.getBasePackage()));
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }

        //生成bean
        GenerateBean generateBean = new GenerateBean(table, fields, srcPackage);
        generateBean.generate();
        ClassName beanClassName = generateBean.out();
        //生成sqlProvider 因为update方法，使用了sql工厂，所以，需要先生成一个sqlProvider
        GenerateSqlProvider generateSqlProvider = new GenerateSqlProvider(beanClassName, table, fields, srcPackage);
        generateSqlProvider.generate();
        ClassName sqlProviderClassName = generateSqlProvider.out();
        //生成mapper
        GenerateMapper generateMapper = new GenerateMapper(beanClassName, table, fields, srcPackage);
        generateMapper.setSqlProviderClassName(sqlProviderClassName);
        generateMapper.generate();
        ClassName mapperClassName = generateMapper.out();
        //生成service
        GenerateService generateService = new GenerateService(beanClassName, table, fields, srcPackage);
        generateService.setMapperClassName(mapperClassName);
        generateService.generate();
        ClassName serviceClassName = generateService.out();
        //生成controller
        GenerateController generateController = new GenerateController(beanClassName, table, fields, srcPackage);
        generateController.setServiceClassName(serviceClassName);
        generateController.generate();
        generateController.out();

        generateFixFile();
    }


    /**
     * 生成固定的文件
     */
    private void generateFixFile() {
        List<FileResBean> javaList = FileResList.getInstance().getJavaList();
        javaList.stream().forEach(file -> {
            try {
                //根据具体的配置生成输出路径
                copyFile(file.getInUrl(),
                        PathUtil.SRC_ROOT_DIR + File.separator + PathUtil.package2Path(config.getBasePackage()) + File.separator + file.getOutUrl());

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("拷贝文件[" + file.getOutUrl() + "]出错");
            }
        });
        System.out.println("拷贝脚手架java文件完成");
        List<FileResBean> resList = FileResList.getInstance().getResList();
        resList.stream().forEach(file -> {
            try {
                //根据具体的配置生成输出路径
                copyFile(file.getInUrl(),
                        PathUtil.RES_ROOT_DIR + File.separator + file.getOutUrl());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("拷贝文件[" + file.getOutUrl() + "]出错");
            }
        });
        System.out.println("拷贝基础配置文件完成");
    }

    /**
     * 直接拷贝一些基础的脚手架文件
     */
    private void copyFile(String in, String out) throws IOException {
        System.out.println("拷贝文件:" + out);
        File file = new File(out);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        InputStream inputStream = this.getClass().getResourceAsStream(in);
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        FileOutputStream outputStream = new FileOutputStream(out);

        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            tempOut.write(buffer, 0, len);
        }
        tempOut.close();
        String source = new String(tempOut.toByteArray());
        if (in.endsWith("java")) {
            //java文件需要替换包名
            source = source.replace("${package}", config.getBasePackage());
        }
        outputStream.write(source.getBytes());
        inputStream.close();
        outputStream.close();
    }

}
