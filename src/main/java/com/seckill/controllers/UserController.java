package com.seckill.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seckill.dtos.UserDto;
import com.seckill.enums.BusinessError;
import com.seckill.errors.BusinessException;
import com.seckill.response.CommonReturnType;
import com.seckill.services.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

  @Autowired
  private UserService userService;

  @GetMapping("/{id}")
  public CommonReturnType getUserById(@PathVariable Integer id) throws BusinessException {
    UserDto dto = userService.getUserById(id);

    if (dto == null) {
      throw new BusinessException(BusinessError.USER_NOT_EXIST);
    }
    return CommonReturnType.create(dto);
  }

}
