package com.github.eros.controller.user;

import com.github.eros.common.model.Result;
import com.github.eros.entry.UserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/20 02:18
 */
@RestController
public class UserInfoController {

    @GetMapping("/user/add")
    public Result addUserInfo(@RequestParam("id")Integer id, @RequestParam("name")String name){
        UserInfoHolder.add(new UserInfo(id, name));
        return Result.createSuccess();
    }

    @GetMapping("/user/remove/{id}")
    public Result removeUserInfo(@PathVariable("id")Integer id){
        UserInfoHolder.remove(id);
        return Result.createSuccess();
    }
}
