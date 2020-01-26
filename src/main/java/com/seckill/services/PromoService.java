package com.seckill.services;

import com.seckill.dtos.PromoDto;

public interface PromoService {

  PromoDto getPromoByItemId(Integer itemId);

}
