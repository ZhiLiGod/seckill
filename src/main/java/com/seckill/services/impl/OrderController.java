package com.seckill.services.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seckill.dtos.OrderDto;
import com.seckill.dtos.UserDto;
import com.seckill.enums.BusinessError;
import com.seckill.errors.BusinessException;
import com.seckill.response.CommonReturnType;
import com.seckill.services.OrderService;

@RestController
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController {

  @Autowired
  private OrderService orderService;

  @Autowired
  private HttpServletRequest httpServletRequest;

  @PostMapping("/create/{itemId}/{amount}")
  public CommonReturnType createOrder(@PathVariable final Integer itemId, @PathVariable final Integer amount) throws BusinessException {
    Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

    if (isLogin == null || !isLogin) {
      throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR, "No User Login");
    }

    UserDto userDto = (UserDto) httpServletRequest.getSession().getAttribute("LOGIN_USER");
    OrderDto orderDto = orderService.createOrder(userDto.getId(), itemId, amount);

    return CommonReturnType.create(orderDto);
  }

}
