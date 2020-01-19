package com.seckill.dtos;

import javax.validation.constraints.NotEmpty;

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

  @NotEmpty
  private String name;
  @NotEmpty
  private Byte gender;
  @NotEmpty
  private String telephone;

  private String registerMode;
  private String thirdPartyId;

  @JsonIgnore
  private String encrptPwd;

}
