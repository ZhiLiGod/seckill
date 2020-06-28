package com.seckill.controllers;

import javax.servlet.http.HttpServletRequest;

import com.seckill.rocketmq.MqProducer;
import com.seckill.services.ItemService;
import com.seckill.services.PromoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

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

  @Autowired
  private PromoService promoService;

  @PostMapping("/create/{itemId}/{amount}/{promoId}")
  public CommonReturnType createOrder(@PathVariable final Integer itemId,
    @PathVariable final Integer amount, @PathVariable final Integer promoId, @RequestParam String promoToken)
      throws BusinessException {

    Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

    if (isLogin == null || !isLogin) {
      throw new BusinessException(BusinessError.USER_NOT_LOGIN);
    }

    UserDto userDto = (UserDto) httpServletRequest.getSession().getAttribute("LOGIN_USER");

    if (promoId != null) {
      String promoTokenInRedis = (String) redisTemplate.opsForValue().get("promo_token_" + promoId + "_userId_" + userDto.getId() + "_itemId_" + itemId);

      if (promoTokenInRedis == null) {
        throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR);
      }

      if (!promoTokenInRedis.equals(promoToken)) {
        throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR);
      }
    }

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

  @PostMapping("/generate/token/{itemId}/{promoId}")
  public CommonReturnType generateToken(@PathVariable Integer itemId, @PathVariable Integer promoId) throws BusinessException {
    Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

    if (isLogin == null || !isLogin) {
      throw new BusinessException(BusinessError.USER_NOT_LOGIN);
    }

    UserDto userDto = (UserDto) httpServletRequest.getSession().getAttribute("LOGIN_USER");
    String promoToken = promoService.generateSecondKillToken(promoId, itemId, userDto.getId());

    if (promoToken == null) {
      throw new BusinessException(BusinessError.UNKNOWN_ERROR);
    }

    return CommonReturnType.create(promoToken);
  }

}
