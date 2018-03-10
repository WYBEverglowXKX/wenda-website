package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户服务
 */
@Service
public class UserService {
    @Autowired
    UserDAO userDao;

    @Autowired
    LoginTicketDAO loginTicketDAO;

    /**
     *@Author LeonWang
     *@Description
     * 获取用户信息
     */
    public User getUser(int id){
        return  userDao.selectById(id);
    }

    /**
     *@Author LeonWang
     *@Description
     * 注册sevice层，注册的业务需要判断，用户名密码是否为空，用户名是否已经存在
     * 如果符合要求，创建用户并且创建一个ticket并返回
     */
    public Map<String, Object> register(String username, String password) {
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }
        User user = userDao.selectByName(username);
        if (user!=null){
            map.put("msg","用户名已存在");
            return map;//老哥，这里要返回错误信息controller才能取到呀！！！
        }
        user = new User();
        user.setName(username);
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
        user.setPassword(WendaUtil.MD5(password+user.getSalt()));
        userDao.addUser(user);

        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }

    /**
     *@Author LeonWang
     *@Description
     * 登录service层逻辑 登录用户名密码也不能为空
     * 如果用户名密码正确，就创建一个ticket，并返回给controller
     */
    public Map<String, Object> login(String username, String password) {
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }
        User user = userDao.selectByName(username);
        if (user==null){
            map.put("msg","用户不存在");
            return map;
        }

        if (!WendaUtil.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msg","密码错误");
        }

        String ticket = addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }

    /**
     *@Author LeonWang
     *@Description
     * 创建ticket
     */
    public String addLoginTicket(int userId){
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(userId);
        Date date = new Date();
        date.setTime(date.getTime() + 1000*3600*24);
        ticket.setExpired(date);
        ticket.setStatus(0);
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        loginTicketDAO.addTicket(ticket);
        return ticket.getTicket();
    }

    /**
     *@Author LeonWang
     *@Description
     * 登出，更新ticket的状态为1
     */
    public void logout(String ticket){
        loginTicketDAO.updateStatus(ticket,1);
    }

    public User selectByName(String name) {
        return userDao.selectByName(name);
    }
}
