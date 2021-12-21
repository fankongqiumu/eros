package com.github.eros.controller;


import com.github.eros.common.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fankongqiumu
 * @Description
 * @date 2021/12/17 11:41
 */
@RestController
public class ErosManageController {

    @GetMapping("/test/demo")
    public Result<String> demo(){
        return Result.createSuccessWithData("demo");
    }
}
