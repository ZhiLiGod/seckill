package com.seckill.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.seckill.dtos.ItemDto;
import com.seckill.services.ItemService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.seckill.dao.PromoMapper;
import com.seckill.dtos.PromoDto;
import com.seckill.dtos.PromoDto.PromoStatus;
import com.seckill.models.Promo;
import com.seckill.services.PromoService;

@Service
public class PromoServiceImpl implements PromoService {

  private static final LocalDateTime END_DATE = LocalDateTime.of(2020, 1, 31, 23, 59);

  @Autowired
  private PromoMapper promoMapper;

  @Autowired
  private ItemService itemService;

  @Autowired
  private RedisTemplate redisTemplate;

  @Override
  public PromoDto getPromoByItemId(Integer itemId) {
    Promo promo = promoMapper.selectByItemId(itemId);

    if (promo == null) {
      return null;
    }

    PromoDto promoDto = convertToPromoDto(promo);
    if (LocalDateTime.now().isBefore(promoDto.getStartDate())) {
      promoDto.setStatus(PromoStatus.END);
    } else if (LocalDateTime.now().isAfter(promoDto.getStartDate())) {
      promoDto.setStatus(PromoStatus.NOT_START_YET);
    } else {
      promoDto.setStatus(PromoStatus.PROCESSING);
    }

    return promoDto;
  }

  @Override
  public void publishPromo(Integer promoId) {
    Promo promo = promoMapper.selectByPrimaryKey(promoId);

    if (promo.getItemId() == null || promo.getItemId() == 0) {
      return;
    }

    ItemDto item = itemService.getItemById(promo.getItemId());

    redisTemplate.opsForValue().set("promo_item_stock_" + item.getId(), item.getStock());
  }

  private PromoDto convertToPromoDto(Promo promo) {
    PromoDto promoDto = new PromoDto();
    BeanUtils.copyProperties(promo, promoDto);
    promoDto.setPromoPrice(BigDecimal.valueOf(promo.getPromoPrice()));
    promoDto.setStartDate(promo.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    promoDto.setEndDate(END_DATE);

    return promoDto;
  }

}
