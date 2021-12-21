package com.github.eros.controller.config;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fankongqiumu
 * @Description
 * @date 2021/12/17 11:41
 */
@RestController
public class ConfigManageController {

    @GetMapping("/demo")
    public String demo(){
        return "demo";
    }
}
