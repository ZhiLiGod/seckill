package com.seckill.dtos;

import lombok.Data;

@Data
public class StockLogDto {

  private String id;
  private Integer itemId;
  private Integer amount;
  private Integer status;

}
