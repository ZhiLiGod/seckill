package com.seckill.services;

import java.util.List;

import com.seckill.dtos.ItemDto;
import com.seckill.errors.BusinessException;

public interface ItemService {

  ItemDto createItem(ItemDto item) throws BusinessException;

  List<ItemDto> listItems();

  ItemDto getItemById(Integer id);

}
