package com.wave.carRecord.common;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class GaterecordQueryDTO {
    private Integer id;

    private String device;

    private String code;

    private String name;

    private String plate;

    private String type;

    private String authentication;

    private Date timestamp;
    
    private Date startDate;
    
    private Date endDate;
    
    private Integer pageSize;
    
    private Integer pageNumber;
}
