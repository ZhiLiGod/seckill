package com.seckill.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.seckill.dtos.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.seckill.dao.OrderInfoMapper;
import com.seckill.dao.SequenceInfoMapper;
import com.seckill.dtos.PromoDto.PromoStatus;
import com.seckill.enums.BusinessError;
import com.seckill.errors.BusinessException;
import com.seckill.models.OrderInfo;
import com.seckill.models.SequenceInfo;
import com.seckill.services.ItemService;
import com.seckill.services.OrderService;
import com.seckill.services.PromoService;
import com.seckill.services.UserService;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OrderServiceImpl implements OrderService {

  private static final Integer INVALID_AMOUNT = 99;

  @Autowired
  private ItemService itemService;

  @Autowired
  private UserService userService;

  @Autowired
  private OrderInfoMapper orderInfoMapper;

  @Autowired
  private SequenceInfoMapper sequenceInfoMapper;

  @Autowired
  private PromoService promoService;

  @Override
  @Transactional
  public OrderDto createOrder(Integer userId, Integer itemId, Integer amount, Integer promoId, String stockLogId) throws BusinessException {

    ItemDto itemDto = itemService.getItemByIdInCache(itemId);

    if (amount <= 0 || amount > INVALID_AMOUNT) {
      throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "Invalid Amount");
    }

    PromoDto promoDto = null;
    if (promoId != null) {
      promoDto = promoService.getPromoByItemId(itemId);

      if (promoDto == null) {
        throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "Invalid Promo Id");
      } else if (!PromoStatus.PROCESSING.equals(promoDto.getStatus())) {
        throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "No Promo Running");
      }
    }

    // order success and reduce stock
    boolean result = itemService.reduceStock(itemId, amount);

    if (!result) {
      throw new BusinessException(BusinessError.STOCK_NOT_ENOUGH);
    }

    OrderInfo order = new OrderInfo();
    order.setItemId(itemId);

    if (promoDto != null) {
      order.setItemPrice(promoDto.getPromoPrice().doubleValue());
      order.setOrderPrice(promoDto.getPromoPrice().multiply(BigDecimal.valueOf(amount)).doubleValue());
    } else {
      order.setItemPrice(itemDto.getPrice().doubleValue());
      order.setOrderPrice(itemDto.getPrice().multiply(BigDecimal.valueOf(amount)).doubleValue());
    }
    order.setAmount(amount);
    order.setUserId(userId);

    // generate order id
    order.setId(generateOrderNo());
    orderInfoMapper.insertSelective(order);

    OrderDto orderDto = new OrderDto();
    BeanUtils.copyProperties(order, orderDto);

    // increase sales
    itemService.increaseSales(itemId, amount);

    // update stock log to success
    StockLogDto stockLogDto = new StockLogDto();

    if (stockLogDto == null) {
      throw new BusinessException(BusinessError.UNKNOWN_ERROR);
    }

    stockLogDto.setStatus(2);
    // update
    // save(stockLogDto)

//    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//
//      @Override
//      public void afterCommit() {
//        // async update stock
//        boolean mqResult = itemService.asyncReduceStock(itemId, amount);
//
////        if (!mqResult) {
////          itemService.increaseStock(itemId, amount);
////          throw new BusinessException(BusinessError.MQ_SEND_FAILED);
////        }
//      }
//
//    });




    return orderDto;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  private String generateOrderNo() {
    StringBuilder sb = new StringBuilder();
    LocalDateTime date = LocalDateTime.now();
    String dateStr = date.format(DateTimeFormatter.ISO_DATE).replace("-", "");

    sb.append(dateStr);

    int sequence = 0;
    SequenceInfo sequenceInfo = sequenceInfoMapper.getSequenceByName("order_info");
    sequence = sequenceInfo.getCurrentValue();
    sequenceInfo.setCurrentValue(sequence + sequenceInfo.getStep());
    sequenceInfoMapper.updateByPrimaryKeySelective(sequenceInfo);

    for (int i = 0; i < 6 - sequence; i++) {
      sb.append("0");
    }

    sb.append(sequence);
    sb.append("00");

    return sb.toString();
  }

}
