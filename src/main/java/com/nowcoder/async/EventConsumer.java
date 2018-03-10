package com.nowcoder.async;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.controller.CommentController;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 事务消费者
 */
@Service
public class EventConsumer implements InitializingBean,ApplicationContextAware{

    @Autowired
    JedisAdapter jedisAdapter;

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    //先在consumer里提前定义一个容器，某种事件类型，需要哪些handler来处理
    Map<EventType,List<EventHandler>> config = new HashMap<EventType,List<EventHandler>>();
    //为下面取出系统所有的handler做准备
    ApplicationContext applicationContext;
    @Override
    public void afterPropertiesSet() throws Exception {
        //取出系统中所有的handler
        Map<String,EventHandler> bean = applicationContext.getBeansOfType(EventHandler.class);
        if (bean!=null){
            for (Map.Entry<String,EventHandler> entry : bean.entrySet()){
                //取出该次循环的handler所支持的所有事件类型
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();
                for (EventType type : eventTypes){
                    //看看定义的config容器里是否有该事件类型没有就加上去
                    if (!config.containsKey(type)){
                        config.put(type,new ArrayList<EventHandler>());
                    }
                    //有就把该事件类型注册上本次循环的handler
                    config.get(type).add(entry.getValue());
                }
            }
        }

        //开启线程，从队列中取数据处理
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    String key = RedisKeyUtil.getEventQueueKey();
                    //从producer传入的时间中取，brpop的意思就是如果存在就取，不存在就卡住
                    List<String> enents = jedisAdapter.brpop(0,key);
                    for (String message : enents){
                        //brpop的第一个元素默认是key
                        if (message.equals(key)){
                            continue;
                        }
                        //反序列化取出来的eventModel
                        EventModel eventModel = JSON.parseObject(message,EventModel.class);
                        if (!config.containsKey(eventModel.getType())){
                            logger.error("该事件不能被处理");
                            continue;
                        }
                        for (EventHandler handler : config.get(eventModel.getType())){
                            handler.doHandle(eventModel);
                        }
                    }
                }

            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
