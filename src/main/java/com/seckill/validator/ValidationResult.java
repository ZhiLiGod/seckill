package com.seckill.validator;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ValidationResult {

  private boolean hasErrors = false;
  private Map<String, String> errMsgMap = new HashMap<>();

  public String getErrMsg() {
    return String.join(", ", errMsgMap.values());
  }

}
