package com.wave.carRecord.bean.carGate;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class Gaterecord {
    private Integer id;

    private String device;

    private String code;

    private String name;

    private String plate;

    private String type;

    private String authentication;

    private Date timestamp;
}
