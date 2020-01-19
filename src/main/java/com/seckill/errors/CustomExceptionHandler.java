package com.seckill.errors;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.seckill.enums.BusinessError;
import com.seckill.response.CommonReturnType;

@ControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Object handlerException(HttpServletRequest req, Exception ex) {
    Map<String, Object> response = new HashMap<>();

    if (ex instanceof BusinessException) {
      BusinessException be = (BusinessException) ex;

      response.put("errCode", be.getErrCode());
      response.put("errMsg", be.getErrMsg());
    } else {
      response.put("errCode", BusinessError.UNKNOWN_ERROR.getErrCode());
      response.put("errMsg", BusinessError.UNKNOWN_ERROR.getErrMsg());
    }

    return CommonReturnType.create(response, com.seckill.enums.ResponseStatus.FAILED);
  }

}
