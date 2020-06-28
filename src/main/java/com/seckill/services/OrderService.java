package com.seckill.services;

import com.seckill.dtos.OrderDto;
import com.seckill.errors.BusinessException;

public interface OrderService {

  OrderDto createOrder(Integer userId, Integer itemId, Integer amount, Integer promoId, String stockLogId) throws BusinessException;

}
