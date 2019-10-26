package com.wave.carRecord.exception;

import com.wave.carRecord.common.CodeMsg;
import com.wave.carRecord.common.ObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获500异常
     * 
     * @param e
     * @return
     */
    @ExceptionHandler
    public ObjectResult<Object> handle500Exception(Exception e) {
    	e.printStackTrace();
        logger.error(e.getMessage(), e);
        return ObjectResult.newFailure(CodeMsg.SERVER_EXCEPTION);
    }
}
