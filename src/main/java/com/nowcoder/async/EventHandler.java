package com.nowcoder.async;

import java.util.List;

/**
 * 事务接口
 */
public interface EventHandler {
    void doHandle(EventModel model);
    List<EventType> getSupportEventTypes();
}
