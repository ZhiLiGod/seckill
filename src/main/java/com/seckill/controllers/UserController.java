package com.seckill.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seckill.dao.UserInfoMapper;
import com.seckill.models.UserInfo;

@RestController
@RequestMapping("/user")
public class UserController {

  @Autowired
  private UserInfoMapper userInfoMapper;

  @GetMapping
  public String getUserName() {
    UserInfo ui = userInfoMapper.selectByPrimaryKey(1);

    if (ui == null) {
      return "User is not exist.";
    }

    return ui.getName();
  }

}
