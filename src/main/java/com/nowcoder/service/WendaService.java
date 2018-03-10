package com.nowcoder.service;

import org.springframework.stereotype.Service;

/**
 * 消息服务
 */
@Service
public class WendaService {
    public String getMessage(int userId) {
        return "Hello Message:" + String.valueOf(userId);
    }
}
