package com.seckill.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

import java.util.concurrent.TimeUnit;

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/item")
public class ItemController {

  private final ItemService itemService;

  private final RedisTemplate redisTemplate;

  public ItemController(ItemService itemService, RedisTemplate redisTemplate) {
    this.itemService = itemService;
    this.redisTemplate = redisTemplate;
  }

  @PostMapping("/create")
  public CommonReturnType createItem(@RequestBody ItemDto itemDto) throws BusinessException {
    itemDto = itemService.createItem(itemDto);
    return CommonReturnType.create(itemDto);
  }

  @GetMapping("/{id}")
  public CommonReturnType getItemById(@PathVariable final Integer id) {

    // find from cache first
    ItemDto itemDto = (ItemDto) redisTemplate.opsForValue().get("item_" + id);

    if (itemDto == null) {
      itemDto = itemService.getItemById(id);

      // save cache
      redisTemplate.opsForValue().set("item_" + id, itemDto);
      redisTemplate.expire("item_" + id, 10, TimeUnit.MINUTES);
    }

    return CommonReturnType.create(itemDto);
  }

  @GetMapping("/all-items")
  public CommonReturnType getAllItems() {
    return CommonReturnType.create(itemService.listItems());
  }

}
