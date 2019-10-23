package com.wave.carRecord.common;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class GaterecordVO {
    private Integer id;

    private String device;
    
    /**
     * 车主姓名
     */
    private String name;
    
    /**
     * 车牌号
     */
    private String plate;
    
    /**
     * 进出口 in/out
     */
    private String type;
    
    /**
     * 授权类型
     */
    private String authentication;
    
    /**
     * 进入时间
     */
    private Date timestamp;
}
