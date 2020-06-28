package com.seckill.services;

import com.seckill.dtos.PromoDto;
import com.seckill.errors.BusinessException;

public interface PromoService {

  PromoDto getPromoByItemId(Integer itemId);

  // publish promo
  void publishPromo(Integer promoId);

  // generate seckill token
  String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) throws BusinessException;

}
