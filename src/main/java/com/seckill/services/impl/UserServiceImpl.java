package com.seckill.services.impl;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.util.StringUtils;
import com.seckill.dao.UserInfoMapper;
import com.seckill.dao.UserPasswordMapper;
import com.seckill.dtos.LoginDto;
import com.seckill.dtos.UserDto;
import com.seckill.enums.BusinessError;
import com.seckill.errors.BusinessException;
import com.seckill.models.UserInfo;
import com.seckill.models.UserPassword;
import com.seckill.services.UserService;
import com.seckill.validator.ValidationResult;
import com.seckill.validator.ValidatorImpl;

import lombok.NonNull;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserInfoMapper userInfoMapper;

  @Autowired
  private UserPasswordMapper userPasswordMapper;

  @Autowired
  private ValidatorImpl validatorImpl;

  @Autowired
  private RedisTemplate redisTemplate;

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
  public void register(@NonNull UserDto user) throws BusinessException {
    ValidationResult valid = validatorImpl.validate(user);

    if (valid.isHasErrors()) {
      throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, valid.getErrMsg());
    }

    UserInfo ui = convertUserInfo(user);
    UserPassword up = convertUserPassword(user);

    try {
      userInfoMapper.insertSelective(ui);
    } catch (DuplicateKeyException e) {
      throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "Duplicate Telephone");
    }

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

  @Override
  public void login(@Valid @NonNull LoginDto loginDto) throws BusinessException {
    UserInfo ui = userInfoMapper.selectByTelephone(loginDto.getTelephone());

    if (ui == null) {
      throw new BusinessException(BusinessError.USER_LOGIN_FAILED);
    }

    UserPassword up = userPasswordMapper.selectByUserId(ui.getId());

    if (!StringUtils.equals(up.getEncrptPassword(), loginDto.getPassword())) {
      throw new BusinessException(BusinessError.USER_LOGIN_FAILED);
    }
  }

  @Override
  public UserDto getUserByIdInCache(Integer id) {

    UserDto user = (UserDto) redisTemplate.opsForValue().get("user_validate_" + id);

    if (user == null) {
      user = getUserById(id);
      redisTemplate.opsForValue().set("user_validate_" + id, user, 10, TimeUnit.MINUTES);
    }

    return user;
  }

}
