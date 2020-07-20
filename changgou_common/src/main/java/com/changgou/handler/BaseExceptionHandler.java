package com.changgou.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * 统一异常处理类
 */
//@ControllerAdvice //声明该类是一个增强类
public class BaseExceptionHandler {
/*
    Logger logger = LoggerFactory.getLogger(BaseExceptionHandler.class);

    static HashMap<Class<? extends Exception>, CustomizeErrorCodeImpl> eMap =
            new HashMap<>();

    static {
        eMap.put(TimeoutException.class, CustomizeErrorCodeImpl.TIMEOUT);
        // 把你觉得需要单独维护的异常-提示 维护到map
    }


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result error(Exception e) {
        e.printStackTrace();
        return new Result(false, StatusCode.ERROR, "当前系统繁忙,请您稍后重试");
    }

    @ExceptionHandler(value = CustomizeException.class)
    @ResponseBody
    public Result error(CustomizeException e) {
        return new Result(e);
    }
*/

}
