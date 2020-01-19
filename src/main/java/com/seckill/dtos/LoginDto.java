package com.seckill.dtos;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class LoginDto {

  @NotEmpty
  private String telephone;

  @NotEmpty
  private String password;

}
