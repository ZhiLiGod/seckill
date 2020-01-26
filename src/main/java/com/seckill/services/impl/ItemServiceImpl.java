package com.seckill.services.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    int affectedRow = stockMapper.reduceStock(itemId, amount);
    return affectedRow > 0;
  }

  @Override
  @Transactional
  public void increaseSales(Integer itemId, Integer amount) {
    itemMapper.increaseSales(itemId, amount);
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
