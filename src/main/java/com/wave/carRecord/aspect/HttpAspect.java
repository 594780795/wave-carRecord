package com.wave.carRecord.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;


@Aspect
@Component
@Slf4j
public class HttpAspect {
    @Around("execution(* com.wave.carRecord.service..*.*(..))")
    public Object logTome(ProceedingJoinPoint pjp) throws Throwable {
        Long startTime = System.currentTimeMillis();
        Object obj =pjp.proceed(pjp.getArgs());
        String declaringTypeName = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();
        String callObject = declaringTypeName+"."+methodName;
        log.info("{} time: {}",callObject,(System.currentTimeMillis()-startTime)+"ms");
        return obj;
    }
}
