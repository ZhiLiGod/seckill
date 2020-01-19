package com.seckill.validator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class ValidatorImpl implements InitializingBean {

  private Validator validator;

  public ValidationResult validate(Object bean) {
    ValidationResult result = new ValidationResult();
    Set<ConstraintViolation<Object>> validateResult = validator.validate(bean);

    if (!validateResult.isEmpty()) {
      result.setHasErrors(Boolean.TRUE);
      validateResult.forEach(v -> {
        String errMsg = v.getMessage();
        String propertyName = v.getPropertyPath().toString();
        result.getErrMsgMap().put(propertyName, errMsg);
      });
    }

    return result;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

}
