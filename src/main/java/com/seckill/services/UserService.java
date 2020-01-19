package com.seckill.services;

import com.seckill.dtos.LoginDto;
import com.seckill.dtos.UserDto;
import com.seckill.errors.BusinessException;

public interface UserService {

  UserDto getUserById(Integer id);

  void register(UserDto user) throws BusinessException;

  void login(LoginDto loginDto) throws BusinessException;

}
