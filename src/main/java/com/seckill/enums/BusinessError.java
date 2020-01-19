package com.seckill.enums;

import com.seckill.errors.CommonError;

public enum BusinessError implements CommonError {
  // @formatter:off
  PARAMETER_VALIDATION_ERROR(10001, "Invalid parameter"),
  UNKNOWN_ERROR(10002, "Unknown error"),
  USER_NOT_EXIST(20001, "User not exist");
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
