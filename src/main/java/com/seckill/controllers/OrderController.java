package com.seckill.controllers;

import javax.servlet.http.HttpServletRequest;

import com.seckill.rocketmq.MqProducer;
import com.seckill.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seckill.dtos.OrderDto;
import com.seckill.dtos.UserDto;
import com.seckill.enums.BusinessError;
import com.seckill.errors.BusinessException;
import com.seckill.response.CommonReturnType;
import com.seckill.services.OrderService;

@RestController
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController {

  @Autowired
  private OrderService orderService;

  @Autowired
  private HttpServletRequest httpServletRequest;

  @Autowired
  private MqProducer mqProducer;

  @Autowired
  private ItemService itemService;

  @Autowired
  private RedisTemplate redisTemplate;

  @PostMapping("/create/{itemId}/{amount}/{promoId}")
  public CommonReturnType createOrder(@PathVariable final Integer itemId, @PathVariable final Integer amount, @PathVariable final Integer promoId)
      throws BusinessException {
    Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

    if (isLogin == null || !isLogin) {
      throw new BusinessException(BusinessError.USER_NOT_LOGIN);
    }

    UserDto userDto = (UserDto) httpServletRequest.getSession().getAttribute("LOGIN_USER");
    // OrderDto orderDto = orderService.createOrder(userDto.getId(), itemId, amount, promoId);

    boolean soldOut = redisTemplate.hasKey("promo_item_sold_out_" + itemId);

    if (soldOut) {
      throw new BusinessException(BusinessError.STOCK_NOT_ENOUGH);
    }

    String stockLogId = itemService.initStockLog(itemId, amount);

    if (!mqProducer.transactionAsyncReduceStock(userDto.getId(), itemId, amount, promoId, stockLogId)) {
      throw new BusinessException(BusinessError.UNKNOWN_ERROR, "Place order failed");
    }

    return CommonReturnType.create(null);
  }

}
