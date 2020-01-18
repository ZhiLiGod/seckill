package com.seckill.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

  private Integer id;
  private String name;
  private Byte gender;
  private String telephone;
  private String registerMode;
  private String thirdPartyId;

  @JsonIgnore
  private String encrptPwd;

}
