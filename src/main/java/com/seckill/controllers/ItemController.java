package com.seckill.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seckill.dtos.ItemDto;
import com.seckill.errors.BusinessException;
import com.seckill.response.CommonReturnType;
import com.seckill.services.ItemService;

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/item")
public class ItemController {

  @Autowired
  private ItemService itemService;

  @PostMapping("/create")
  public CommonReturnType createItem(@RequestBody ItemDto itemDto) throws BusinessException {
    itemDto = itemService.createItem(itemDto);
    return CommonReturnType.create(itemDto);
  }

  @GetMapping("/{id}")
  public CommonReturnType getItemById(@PathVariable final Integer id) {
    return CommonReturnType.create(itemService.getItemById(id));
  }

}
