package com.seckill.dtos;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderDto {

  private String id;
  private Integer userId;
  private Integer itemId;
  private BigDecimal itemPrice;
  private Integer amount;
  private BigDecimal orderPrice;

}
