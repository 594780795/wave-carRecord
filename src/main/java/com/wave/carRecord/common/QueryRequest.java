package com.wave.carRecord.common;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author mintc
 * @projectName car-record
 * @description: TODO
 * @date 2019/10/2114:37
 */
@Data
@ToString
public class QueryRequest {
    
    /**
     * 车牌号
     */
    private String plate;
    
    /**
     * 进出口 in / out
     */
    private String type;
    
    /**
     * 授权
     */
    private String authentication;
    
    /**
     * 导出类型 1全部 2当前页
     */
    private String exportType;
    
    /**
     * 开始时间
     */
    private String startDate;
    
    /**
     * 结束时间
     */
    private String endDate;
    
    /**
     * 第几页
     */
    private Integer pageSize;
    
    /**
     * 每页记录数
     */
    private Integer pageNumber;
    
    /**
     * 验证码
     */
    private String validCode;
}
