package com.wave.carRecord.service;

import com.wave.carRecord.common.*;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author mintc
 * @projectName car-record
 * @description: TODO
 * @date 2019/10/2017:41
 */
public interface GaterecordService {
    /**
     * 查询车辆进出记录
     * @param req
     * @return
     */
    ObjectResult<GaterecordResponse> queryCarRecord(QueryRequest req) throws ParseException;
    
    /**
     * excel导出道闸的车辆进出历史记录信息
     * @param queryRequest
     * @param response
     */
    void exportCarRecordExcel(QueryRequest queryRequest, HttpServletResponse response) throws Exception;
}
