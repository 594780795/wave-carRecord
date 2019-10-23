package com.wave.carRecord.common;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@ToString
public class GaterecordResponse {
    
    private int total;
    
    private int currPage;
    
    private int currCount;
    
    private int totalPage;
    
    private List<GaterecordVO> gaterecordVOList;
}
