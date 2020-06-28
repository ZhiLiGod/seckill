package com.seckill.rocketmq;

import com.alibaba.fastjson.JSON;
import com.seckill.dtos.StockLogDto;
import com.seckill.errors.BusinessException;
import com.seckill.services.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {

  private DefaultMQProducer producer;

  private TransactionMQProducer transactionMQProducer;

  @Value("${mq.nameserver.addr}")
  private String nameAddr;

  @Value("${mq.topicname}")
  private String topicName;

  @Autowired
  private OrderService orderService;

  @PostConstruct
  public void init() throws MQClientException {
    producer = new DefaultMQProducer("producer_group");
    producer.setNamesrvAddr(nameAddr);
    producer.start();

    transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
    transactionMQProducer.setNamesrvAddr(nameAddr);
    transactionMQProducer.start();

    transactionMQProducer.setTransactionListener(new TransactionListener() {

      @Override
      public LocalTransactionState executeLocalTransaction(Message message, Object args) {

        Integer itemId = (Integer) ((Map) args).get("itemId");
        Integer userId = (Integer) ((Map) args).get("userId");
        Integer promoId = (Integer) ((Map) args).get("promoId");
        Integer amount = (Integer) ((Map) args).get("amount");
        String stockLogId = (String) ((Map) args).get("stockLogId");

        // create order
        try {
          orderService.createOrder(userId, itemId, amount, promoId, stockLogId);
        } catch (BusinessException e) {
          e.printStackTrace();

          // set stocklog to rollback
          StockLogDto stockLogDto = new StockLogDto();
          stockLogDto.setStatus(3);
          // update

          return LocalTransactionState.ROLLBACK_MESSAGE;
        }

        return LocalTransactionState.COMMIT_MESSAGE;
      }

      @Override
      public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {

        // this callback happens when createOrder takes too long or server down
        String jsonString = new String(messageExt.getBody());
        Map<String, Object> map = JSON.parseObject(jsonString, Map.class);
        Integer itemId = (Integer) map.get("itemId");
        Integer amount = (Integer) map.get("amount");
        String stockLogId = (String) map.get("stockLogId");

        StockLogDto stockLogDto = new StockLogDto();

        if (stockLogDto == null) {
          return LocalTransactionState.UNKNOW;
        }

        if (stockLogDto.getStatus() == 2) {
          return LocalTransactionState.COMMIT_MESSAGE;
        } else if (stockLogDto.getStatus() == 1) {
          return LocalTransactionState.UNKNOW;
        }

        return LocalTransactionState.ROLLBACK_MESSAGE;
      }

    });
  }

  // transaction sync reduction
  public boolean transactionAsyncReduceStock(Integer itemId, Integer amount, Integer userId, Integer promoId, String stockLogId) {
    Map<String, Object> bodyMap = new HashMap<>();
    bodyMap.put("itemId", itemId);
    bodyMap.put("amount", amount);
    bodyMap.put("stockLogId", stockLogId);

    Map<String, Object> argsMap = new HashMap<>();
    argsMap.put("itemId", itemId);
    argsMap.put("amount", amount);
    argsMap.put("userId", userId);
    argsMap.put("promoId", promoId);
    argsMap.put("stockLogId", stockLogId);

    Message message = new Message(topicName, "increase",
      JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));

    TransactionSendResult sendResult = null;
    try {
      sendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
    } catch (MQClientException e) {
      e.printStackTrace();
    }

    if (LocalTransactionState.ROLLBACK_MESSAGE.equals(sendResult.getLocalTransactionState())) {
      return false;
    } else
      return LocalTransactionState.COMMIT_MESSAGE.equals(sendResult.getLocalTransactionState());
  }

  // sync stock reduction
  public boolean asyncReduceStock(Integer itemId, Integer amount) {
    Map<String, Object> bodyMap = new HashMap<>();

    bodyMap.put("itemId", itemId);
    bodyMap.put("amount", amount);
    Message message = new Message(topicName, "increase",
      JSON.toJSON(bodyMap).toString().getBytes(StandardCharsets.UTF_8));

    try {
      producer.send(message);
    } catch (MQClientException | InterruptedException | MQBrokerException | RemotingException e) {
      return false;
    }

    return true;
  }

}
