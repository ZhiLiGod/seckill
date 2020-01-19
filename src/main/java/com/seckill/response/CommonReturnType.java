package com.seckill.response;

import com.seckill.enums.ResponseStatus;

import lombok.Data;

@Data
public class CommonReturnType {

  private String status;
  private Object data;

  public static CommonReturnType create() {
    return create(null, ResponseStatus.SUCCESS);
  }

  public static CommonReturnType create(Object result) {
    return create(result, ResponseStatus.SUCCESS);
  }

  public static CommonReturnType create(Object result, ResponseStatus status) {
    CommonReturnType crt = new CommonReturnType();

    crt.setData(result);
    crt.setStatus(status.name());

    return crt;
  }

}
