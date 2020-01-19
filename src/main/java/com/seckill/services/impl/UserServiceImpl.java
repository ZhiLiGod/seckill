package com.seckill.services.impl;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seckill.dao.UserInfoMapper;
import com.seckill.dao.UserPasswordMapper;
import com.seckill.dtos.UserDto;
import com.seckill.models.UserInfo;
import com.seckill.models.UserPassword;
import com.seckill.services.UserService;

import lombok.NonNull;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserInfoMapper userInfoMapper;

  @Autowired
  private UserPasswordMapper userPasswordMapper;

  @Override
  public UserDto getUserById(Integer id) {
    UserInfo ui = userInfoMapper.selectByPrimaryKey(id);
    UserPassword up = null;

    if (ui != null) {
      up = userPasswordMapper.selectByUserId(ui.getId());
    }

    return convertFromDataObject(ui, up);
  }

  private UserDto convertFromDataObject(@NonNull UserInfo ui, UserPassword up) {
    UserDto dto = new UserDto();
    BeanUtils.copyProperties(ui, dto);

    if (up != null) {
      dto.setEncrptPwd(up.getEncrptPassword());
    }

    return dto;
  }

  @Override
  @Transactional
  public void register(@Valid @NonNull UserDto user) {
    UserInfo ui = convertUserInfo(user);
    UserPassword up = convertUserPassword(user);

    userInfoMapper.insertSelective(ui);
    userPasswordMapper.insertSelective(up);
  }

  private UserInfo convertUserInfo(UserDto dto) {
    UserInfo ui = new UserInfo();
    BeanUtils.copyProperties(dto, ui);

    return ui;
  }

  private UserPassword convertUserPassword(UserDto dto) {
    UserPassword up = new UserPassword();
    up.setEncrptPassword(dto.getEncrptPwd());
    up.setUserId(dto.getId());

    return up;
  }

}
