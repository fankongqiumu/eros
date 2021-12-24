package com.github.eros.controller.config;


import com.github.eros.common.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fankongqiumu
 * @Description
 * @date 2021/12/17 11:41
 */
@RequestMapping("/config/manage")
@RestController
public class ConfigManageController {

    @GetMapping("/demo")
    public Result demo(){
        Result<Object> result = Result.createSuccess();
        result.setData("demo");
        return result;
    }
}
