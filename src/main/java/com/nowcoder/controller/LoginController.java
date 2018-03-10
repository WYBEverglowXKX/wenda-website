package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jws.WebParam;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 登录
 */
@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;
    /**
     *@Author LeonWang
     *@Description
     * 用来跳转到主页面
     */
    @RequestMapping(path = {"/reglogin"}, method = {RequestMethod.GET})
    public String regloginPage(Model model, @RequestParam(value = "next", required = false) String next) {
        model.addAttribute("next", next);
        return "login";
    }

    /**
     *@Author LeonWang
     *@Description
     * 登出函数，通过更改ticket的status为1，设置用户为登出的状态
     */
    @RequestMapping(path = {"/logout"},method = {RequestMethod.GET})
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/";
    }



    /**
     *@Author LeonWang
     *@Description
     * 用户注册，处理逻辑，从service层拿到ticket，根据ticket的有无来判断是否注册成功，如果注册成功，就创建一个cookie
     * cookie的值就是ticket，浏览器存储cookie
     * 可能会出现异常，所以try catch
     * rememberme是页面记住我的选项，如果选择记住我，cookie的存活时间增长
     */
    @RequestMapping(path = {"/reg/"}, method = {RequestMethod.POST})
    public String reg(Model model,
                      @RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam(value = "rememberme",defaultValue = "false") boolean rememberme,
                      @RequestParam(value = "next") String next,
                      HttpServletResponse response){
        try {
            Map<String, Object> map = userService.register(username,password);
            if (map.containsKey("ticket")){
                Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
                cookie.setPath("/");
                if (rememberme){
                    cookie.setMaxAge(3600*24*5);
                }
                response.addCookie(cookie);
                if (!StringUtils.isBlank(next)){
                    return "redirect:"+next;
                }
                return "redirect:/";
            }else {
                model.addAttribute("msg",map.get("msg"));
                return "login";
            }
        }catch (Exception e) {
            logger.error("注册异常" + e.getMessage());
            return "login";
        }

    }

    /**
     *@Author LeonWang
     *@Description
     * 用户登录逻辑处理，登录时根据获取的ticket来判断是否登录成功，如果登录成功就在浏览器端存储一个cookie
     * 然后跳转到主页面
     */
    @RequestMapping(path = {"/login/"}, method = {RequestMethod.POST})
    public String login(Model model,
                        @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value = "rememberme",defaultValue = "false") boolean rememberme,
                        @RequestParam(value = "next") String next,
                        HttpServletResponse response){

        try {
            Map<String, Object> map = userService.login(username,password);
            if (map.containsKey("ticket")){
                model.addAttribute("ticket",map.get("ticket"));
                Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
                cookie.setPath("/");
                if (rememberme){
                    cookie.setMaxAge(3600*24*5);
                }
                response.addCookie(cookie);



                if (!StringUtils.isBlank(next)){
                    return "redirect:"+next;
                }
                return "redirect:/";
            }else {
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }
        }catch (Exception e) {
            logger.error("注册异常" + e.getMessage());
            model.addAttribute("msg", "服务器错误");
            return "login";
        }

    }
}
