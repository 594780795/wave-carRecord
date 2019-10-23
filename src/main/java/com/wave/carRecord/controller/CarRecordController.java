package com.wave.carRecord.controller;

import com.alibaba.fastjson.JSON;
import com.wave.carRecord.common.*;
import com.wave.carRecord.service.GaterecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author mintc
 * @projectName car-record
 * @description: 车辆记录
 * @date 2019/10/2016:31
 */
@Component
@RequestMapping("car")
public class CarRecordController {
    
    private final Logger logger = LoggerFactory.getLogger(CarRecordController.class);
    
    @Autowired
    GaterecordService gaterecordService;
    
    @ResponseBody
    @RequestMapping(value = "queryCarRecord", method = RequestMethod.POST)
    public ObjectResult<GaterecordResponse> queryCarRecord (@RequestBody QueryRequest req) {
        logger.info("car record params: " + JSON.toJSONString(req));
        ObjectResult<GaterecordResponse> result = null;
        try{
            result = gaterecordService.queryCarRecord(req);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return result;
    }
    
    @RequestMapping(value = "excel", method = RequestMethod.POST)
    public void excel(@RequestBody QueryRequest queryRequest, HttpServletResponse response) {
        logger.info("car/excel params:", JSON.toJSONString(queryRequest));
        try{
            gaterecordService.exportCarRecordExcel(queryRequest, response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }
}
