package com.seckill.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PromoDto {

  public enum PromoStatus {
    NOT_START_YET, PROCESSING, END;
  }

  private Integer id;
  private String promoName;
  private LocalDateTime startDate;
  private Integer itemId;
  private BigDecimal promoPrice;
  private LocalDateTime endDate;
  private PromoStatus status;

}
