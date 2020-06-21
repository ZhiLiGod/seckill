package com.seckill.services;

/**
 * Guava cache
 */
public interface CacheService {

  void setCommonCache(String key, Object value);

  Object getFromCommonCache(String key);

}
