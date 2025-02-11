package com.pioneer.agro_claim.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAdvice {
    Logger logger = LoggerFactory.getLogger(LoggingAdvice.class);

    @Pointcut(value = "execution(* com.pioneer.agro_claim.*.*(..))")
    public void myPointCut() {
    }

    @Around("myPointCut()")
    public Object applicationLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        ObjectMapper mapper = new ObjectMapper();
        String methodName = proceedingJoinPoint.getSignature().getName();
        String className = proceedingJoinPoint.getTarget().getClass().toString();
        Object[] arrayOfArgs = proceedingJoinPoint.getArgs();
        try {
            logger.info("method invoked from " + className + " is : " + methodName + "() with arguments : " + mapper.writeValueAsString(arrayOfArgs));
            Object responseObject = proceedingJoinPoint.proceed();
            logger.info("Response from method name " + methodName + "() present in class name " + className + " is " + mapper.writeValueAsString(responseObject));
            return responseObject;
        } catch (Exception e){
            logger.error("Exception in method " + methodName + "() in class " + className, e);
            throw e;
        }
    }
}
