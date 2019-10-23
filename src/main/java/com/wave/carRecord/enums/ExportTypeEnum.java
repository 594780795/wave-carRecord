package com.wave.carRecord.enums;

/**
 * @author: lenovo
 * @date: 2019/5/31 15:54
 */
public enum ExportTypeEnum {

    /**
     * 表格限制
     */
    ALL("1"),
    CURRENT("2");
    
    private String code;
    
    ExportTypeEnum(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
}
