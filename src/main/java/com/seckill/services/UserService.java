package com.seckill.services;

import com.seckill.dtos.UserDto;

public interface UserService {

  UserDto getUserById(Integer id);

  void register(UserDto user);

}
