package com.zjiecode.web.generator.bean;

/**
 * 一个表的每个字段的描述信息
 */
public class FieldBean {
    private String name;
    private String comment;
    private String type;
    private int length;
    private boolean canNull;
    private boolean isPrimary;
    private boolean isAutoIncrement;

    public FieldBean(String name, String comment, String type, int length, boolean canNull, boolean isPrimary, boolean isAutoIncrement) {
        this.name = name;
        this.comment = comment;
        this.type = type;
        this.length = length;
        this.canNull = canNull;
        this.isPrimary = isPrimary;
        this.isAutoIncrement = isAutoIncrement;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isCanNull() {
        return canNull;
    }

    public void setCanNull(boolean canNull) {
        this.canNull = canNull;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
    }

    public boolean isCommentEmpty() {
        return getComment() == null || getComment().length() == 0;
    }
}
