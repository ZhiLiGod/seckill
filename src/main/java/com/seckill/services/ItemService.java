package com.seckill.services;

import java.util.List;

import com.seckill.dtos.ItemDto;
import com.seckill.errors.BusinessException;

public interface ItemService {

  ItemDto createItem(ItemDto item) throws BusinessException;

  List<ItemDto> listItems();

  ItemDto getItemById(Integer id);

  boolean reduceStock(Integer itemId, Integer amount);

  void increaseSales(Integer itemId, Integer amount);

  // validate item and promo
  ItemDto getItemByIdInCache(Integer id);

  // async update stock
  boolean asyncReduceStock(Integer itemId, Integer amount);

  // rollback stock
  boolean increaseStock(Integer itemId, Integer amount);

  String initStockLog(Integer itemId, Integer amount);

}
