package com.seckill.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.seckill.dtos.ItemDto;
import com.seckill.dtos.UserDto;
import com.seckill.enums.BusinessError;
import com.seckill.errors.BusinessException;
import com.seckill.services.ItemService;
import com.seckill.services.UserService;
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

  @Autowired
  private UserService userService;

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

    // set threshold for promo item
    redisTemplate.opsForValue().set("promo_threshold_count_" + promoId, item.getStock() * 5);
  }

  @Override
  public String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) throws BusinessException {

    boolean soldOut = redisTemplate.hasKey("promo_item_sold_out_" + itemId);

    if (soldOut) {
      return null;
    }

    Promo promo = promoMapper.selectByPrimaryKey(promoId);

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

    if (!PromoStatus.PROCESSING.equals(promoDto.getStatus())) {
      return null;
    }

    ItemDto itemDto = itemService.getItemByIdInCache(itemId);

    if (itemDto == null) {
      return null;
    }

    UserDto userDto = userService.getUserByIdInCache(userId);

    if (userDto == null) {
      return null;
    }

    String token = UUID.randomUUID().toString().replace("-", "");

    // get promo item threshold
    long result = redisTemplate.opsForValue().increment("promo_threshold_count_" + promoId, -1);

    if (result < 0) {
      return null;
    }

    redisTemplate.opsForValue().set("promo_token_" + promoId + "_userId_" + userId + "_itemId_" + itemId, token, 5, TimeUnit.MINUTES);

    return token;
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
