package com.seckill.enums;

import com.seckill.errors.CommonError;

public enum BusinessError implements CommonError {
  // @formatter:off
  PARAMETER_VALIDATION_ERROR(10001, "Invalid parameter"),
  UNKNOWN_ERROR(10002, "Unknown error"),
  USER_NOT_EXIST(20001, "User not exist"),
  USER_LOGIN_FAILED(20002, "Invalid Telephone or Password"),
  USER_NOT_LOGIN(20003, "User Not Login"),
  STOCK_NOT_ENOUGH(30001, "Stock Not Enough"),
  MQ_SEND_FAILED(30002, "Stock async message failed"),
  RATE_LIMITER_ERROR(30003, "Hit item, please try again later");
  // @formatter:on

  private int errCode;
  private String errMsg;

  private BusinessError(int errCode, String errMsg) {
    this.errCode = errCode;
    this.errMsg = errMsg;
  }

  @Override
  public int getErrCode() {
    return errCode;
  }

  @Override
  public String getErrMsg() {
    return errMsg;
  }

  @Override
  public CommonError setErrMsg(String errMsg) {
    this.errMsg = errMsg;
    return this;
  }

}
