package com.seckill.services.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.seckill.services.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CacheServiceImpl implements CacheService {

  private Cache<String, Object> commonCache = null;

  @PostConstruct
  public void init() {
    commonCache = CacheBuilder.newBuilder()
      // 初始容量
      .initialCapacity(10)
      // 超过100会按照LRU移除缓存项
      .maximumSize(100)
      .expireAfterWrite(60, TimeUnit.SECONDS)
      .build();
  }

  @Override
  public void setCommonCache(String key, Object value) {
    commonCache.put(key, value);
  }

  @Override
  public Object getFromCommonCache(String key) {
    return commonCache.getIfPresent(key);
  }

}
