package com.seckill.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.seckill.dao.OrderInfoMapper;
import com.seckill.dao.SequenceInfoMapper;
import com.seckill.dtos.ItemDto;
import com.seckill.dtos.OrderDto;
import com.seckill.dtos.UserDto;
import com.seckill.enums.BusinessError;
import com.seckill.errors.BusinessException;
import com.seckill.models.OrderInfo;
import com.seckill.models.SequenceInfo;
import com.seckill.services.ItemService;
import com.seckill.services.OrderService;
import com.seckill.services.UserService;

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

  @Override
  @Transactional
  public OrderDto createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException {
    // validate
    ItemDto itemDto = itemService.getItemById(itemId);

    if (itemDto == null) {
      throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "Invalid Item Id");
    }

    UserDto userDto = userService.getUserById(userId);

    if (userDto == null) {
      throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "Invalid User Id");
    }

    if (amount <= 0 || amount > INVALID_AMOUNT) {
      throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "Invalid Amount");
    }

    // order success and reduce stock
    boolean result = itemService.reduceStock(itemId, amount);

    if (!result) {
      throw new BusinessException(BusinessError.STOCK_NOT_ENOUGH);
    }

    OrderInfo order = new OrderInfo();
    order.setItemId(itemId);
    order.setAmount(amount);
    order.setUserId(userId);
    order.setItemPrice(itemDto.getPrice().doubleValue());
    order.setOrderPrice(itemDto.getPrice().multiply(BigDecimal.valueOf(amount)).doubleValue());

    // generate order id
    order.setId(generateOrderNo());
    orderInfoMapper.insertSelective(order);

    OrderDto orderDto = new OrderDto();
    BeanUtils.copyProperties(order, orderDto);
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
