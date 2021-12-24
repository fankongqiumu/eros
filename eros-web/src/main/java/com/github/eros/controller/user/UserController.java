package com.github.eros.controller.user;

import com.github.eros.common.model.Result;
import com.github.eros.domain.user.UserInfo;
import org.springframework.web.bind.annotation.*;


/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 02:18
 */
@RequestMapping("/user")
@RestController
public class UserController {

    /**
     * todo mock中 待实现
     * @param userId
     * @param name
     * @return
     */
    @GetMapping("/manage/add")
    public Result addUser(@RequestParam("userId")Long userId, @RequestParam("name")String name){
        UserInfoHolder.add(new UserInfo(userId, name));
        return Result.createSuccess();
    }

    /**
     * todo mock中 待实现
     * @param userId
     * @return
     */
    @GetMapping("/manage/dsable/{id}")
    public Result dsableUser(@PathVariable("userId")Long userId){
        UserInfoHolder.remove(userId);
        return Result.createSuccess();
    }

    /**
     * todo mock中 待实现
     * @param userId
     * @return
     */
    @GetMapping("/manage/enable/{id}")
    public Result enableUser(@PathVariable("userId")Long userId){
        UserInfoHolder.remove(userId);
        return Result.createSuccess();
    }

}
