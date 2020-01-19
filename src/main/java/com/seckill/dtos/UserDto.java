package com.seckill.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

  @NotBlank
  private String name;
  @NotNull
  private Byte gender;
  @NotBlank
  private String telephone;

  private String registerMode;
  private String thirdPartyId;

  @JsonIgnore
  private String encrptPwd;

}
