package com.seckill.controllers;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;
import com.seckill.dtos.LoginDto;
import com.seckill.dtos.UserDto;
import com.seckill.enums.BusinessError;
import com.seckill.errors.BusinessException;
import com.seckill.response.CommonReturnType;
import com.seckill.services.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/user")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private HttpServletRequest httpServletRequest;

  @GetMapping("/{id}")
  public CommonReturnType getUserById(@PathVariable Integer id) throws BusinessException {
    UserDto dto = userService.getUserById(id);

    if (dto == null) {
      throw new BusinessException(BusinessError.USER_NOT_EXIST);
    }
    return CommonReturnType.create(dto);
  }

  @GetMapping("/{telephone}")
  public CommonReturnType getOtp(@PathVariable final String telephone) throws NoSuchAlgorithmException {
    // 1. generate otp code
    Random random = SecureRandom.getInstanceStrong();
    int randomInt = random.nextInt(99999);
    randomInt += 10000;
    String otpCode = String.valueOf(randomInt);

    // 2. associate user telephone with code (use redis later)
    httpServletRequest.getSession().setAttribute(telephone, otpCode);

    // 3. send to user
    log.info("Telephone: " + telephone + " Otp code: " + otpCode);

    return CommonReturnType.create();
  }

  @PostMapping("/register/{otpCode}")
  public CommonReturnType register(@Valid @RequestBody UserDto user, @PathVariable final String otpCode) throws BusinessException {
    String otpCodeInSession = (String) httpServletRequest.getSession().getAttribute(user.getTelephone());

    if (StringUtils.equals(otpCode, otpCodeInSession)) {
      throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "Invalid Otp Code");
    }

    userService.register(user);
    return CommonReturnType.create();
  }

  @PostMapping("/login")
  public CommonReturnType login(@Valid @RequestBody LoginDto loginDto) throws BusinessException {
    userService.login(loginDto);
    httpServletRequest.setAttribute("IS_LOGIN", true);

    return CommonReturnType.create();
  }

}
