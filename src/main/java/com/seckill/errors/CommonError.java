package com.seckill.errors;

public interface CommonError {

  int getErrCode();

  String getErrMsg();

  CommonError setErrMsg(String errMsg);

}
