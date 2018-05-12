package com.zjiecode.web.generator;

/**
 * 生成代码的时候，发生错误
 */
public class GenerateException extends RuntimeException {
    public GenerateException(String message) {
        super(message);
    }
}
