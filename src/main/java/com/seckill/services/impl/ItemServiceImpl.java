package com.seckill.services.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.seckill.dtos.StockLogDto;
import com.seckill.rocketmq.MqProducer;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seckill.dao.ItemMapper;
import com.seckill.dao.StockMapper;
import com.seckill.dtos.ItemDto;
import com.seckill.dtos.PromoDto;
import com.seckill.dtos.PromoDto.PromoStatus;
import com.seckill.enums.BusinessError;
import com.seckill.errors.BusinessException;
import com.seckill.models.Item;
import com.seckill.models.Stock;
import com.seckill.services.ItemService;
import com.seckill.services.PromoService;
import com.seckill.validator.ValidationResult;
import com.seckill.validator.ValidatorImpl;

import lombok.NonNull;

@Service
public class ItemServiceImpl implements ItemService {

  @Autowired
  private ItemMapper itemMapper;

  @Autowired
  private StockMapper stockMapper;

  @Autowired
  private ValidatorImpl validatorImpl;

  @Autowired
  private PromoService promoService;

  @Autowired
  private RedisTemplate redisTemplate;

  @Autowired
  private MqProducer producer;

  @Override
  @Transactional
  public ItemDto createItem(ItemDto itemDto) throws BusinessException {
    ValidationResult validResult = validatorImpl.validate(itemDto);

    if (validResult.isHasErrors()) {
      throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, validResult.getErrMsg());
    }

    Item item = convertItemFromItemDto(itemDto);
    itemMapper.insertSelective(item);
    itemDto.setId(item.getId());

    Stock stock = convertStockFromItemDto(itemDto);
    stockMapper.insertSelective(stock);

    return getItemById(item.getId());
  }

  @Override
  public List<ItemDto> listItems() {
    // @formatter:off
    return itemMapper.selectAll().stream()
        .map(i -> {
          Stock stock = stockMapper.selectByItemId(i.getId());
          return convertFromItemAndStock(i, stock);
        }).collect(Collectors.toList());
    // @formatter:on
  }

  @Override
  public ItemDto getItemById(Integer id) {
    Item item = itemMapper.selectByPrimaryKey(id);

    if (item == null) {
      return null;
    }

    Stock stock = stockMapper.selectByItemId(item.getId());
    return convertFromItemAndStock(item, stock);
  }

  @Override
  @Transactional
  public boolean reduceStock(Integer itemId, Integer amount) {
//    int affectedRow = stockMapper.reduceStock(itemId, amount);
    // reduce from redis
    long result = redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, (amount * -1));

    if (result > 0) {
      return true;
    } else if (result == 0) {
      // set sold out flag
      redisTemplate.opsForValue().set("promo_item_sold_out_" + itemId, "true");
      return true;
    } else {
      increaseStock(itemId, amount);
      return false;
    }
  }

  @Override
  @Transactional
  public void increaseSales(Integer itemId, Integer amount) {
    itemMapper.increaseSales(itemId, amount);
  }

  @Override
  public ItemDto getItemByIdInCache(Integer id) {

    ItemDto item = (ItemDto) redisTemplate.opsForValue().get("item_validate_" + id);

    if (item == null) {
      item = getItemById(id);
      redisTemplate.opsForValue().set("item_validate_" + id, item, 10, TimeUnit.MINUTES);
    }

    return item;
  }

  @Override
  public boolean asyncReduceStock(Integer itemId, Integer amount) {
    return producer.asyncReduceStock(itemId, amount);
  }

  @Override
  public boolean increaseStock(Integer itemId, Integer amount) {
    redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount);
    return true;
  }

  @Override
  @Transactional
  public String initStockLog(Integer itemId, Integer amount) {
    StockLogDto stockLogDto = new StockLogDto();
    stockLogDto.setAmount(amount);
    stockLogDto.setItemId(itemId);
    stockLogDto.setId(UUID.randomUUID().toString().replace("-", ""));
    stockLogDto.setStatus(1);

    return stockLogDto.getId();
  }

  private Item convertItemFromItemDto(@NonNull ItemDto dto) {
    Item item = new Item();
    BeanUtils.copyProperties(dto, item);
    item.setPrice(dto.getPrice().doubleValue());

    return item;
  }

  private Stock convertStockFromItemDto(@NonNull ItemDto dto) {
    Stock stock = new Stock();
    stock.setStock(dto.getStock());
    stock.setItemId(dto.getId());

    return stock;
  }

  private ItemDto convertFromItemAndStock(@NonNull Item item, @NonNull Stock stock) {
    ItemDto dto = new ItemDto();
    BeanUtils.copyProperties(item, dto);

    dto.setPrice(BigDecimal.valueOf(item.getPrice()));
    dto.setStock(stock.getStock());

    PromoDto promoDto = promoService.getPromoByItemId(item.getId());

    if (promoDto != null && !PromoStatus.END.equals(promoDto.getStatus())) {
      dto.setPromoDto(promoDto);
    }

    return dto;
  }

}
