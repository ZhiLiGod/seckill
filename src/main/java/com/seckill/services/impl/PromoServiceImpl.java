package com.seckill.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

  private PromoDto convertToPromoDto(Promo promo) {
    PromoDto promoDto = new PromoDto();
    BeanUtils.copyProperties(promo, promoDto);
    promoDto.setPromoPrice(BigDecimal.valueOf(promo.getPromoPrice()));
    promoDto.setStartDate(promo.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    promoDto.setEndDate(END_DATE);

    return promoDto;
  }

}
