package com.changgou.Exception.impl;


import com.changgou.Exception.CustomizeErrorCode;

public enum CustomizeErrorCodeImpl implements CustomizeErrorCode {

  TIMEOUT(1111,"超时"),
  Q_N(4004,"查询失败")

    // 参数不合法
    ;

    private String message;
    private Integer code;
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode() {
        return code;
    }
    CustomizeErrorCodeImpl(Integer code, String message){
        this.code=code;
        this.message=message;}


}