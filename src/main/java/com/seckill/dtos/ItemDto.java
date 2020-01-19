package com.seckill.dtos;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ItemDto {

  private Integer id;

  @NotBlank(message = "Cannot be empty")
  private String title;

  @NotNull(message = "Cannot be empty")
  @Min(value = 0, message = "Price must greater than 0")
  private BigDecimal price;

  @NotNull(message = "Cannot be null")
  private Integer stock;

  @NotBlank(message = "Cannot be empty")
  private String description;

  private Integer sales;

  @NotBlank(message = "Cannot be empty")
  private String imageUrl;

}
