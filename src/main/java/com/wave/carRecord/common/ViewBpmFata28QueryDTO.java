package com.wave.carRecord.common;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class ViewBpmFata28QueryDTO {
    
    private Integer id;
    
    private Integer run_id;
    
    private String run_name;
    
    private String begin_user;
    
    private Date begin_time;
    
    private Integer flow_auto_num;
    
    private Integer flow_auto_num_year;
    
    private Integer flow_auto_num_month;
    /**
     * 车牌号
     */
    private String data_m664;
    
    /**
     * 车辆进出时间
     */
    private Date timestamp;
}
