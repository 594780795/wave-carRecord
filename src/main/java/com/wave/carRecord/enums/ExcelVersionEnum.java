package com.wave.carRecord.enums;

/**
 * @author: lenovo
 * @date: 2019/5/31 15:54
 */
public enum ExcelVersionEnum {

    /**
     * 表格限制
     */
    V2003("xls", 10000, 100),
    V2007("xlsx", 100, 100);

    private String suffix;

    private int maxRow;

    private int maxColumn;



    ExcelVersionEnum(String suffix, int maxRow, int maxColumn) {
        this.suffix = suffix;
        this.maxRow = maxRow;
        this.maxColumn = maxColumn;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getMaxRow() {
        return maxRow;
    }

    public void setMaxRow(int maxRow) {
        this.maxRow = maxRow;
    }

    public int getMaxColumn() {
        return maxColumn;
    }

    public void setMaxColumn(int maxColumn) {
        this.maxColumn = maxColumn;
    }
}
