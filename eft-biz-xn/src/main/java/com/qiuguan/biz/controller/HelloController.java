package com.qiuguan.biz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @author fu yuan hui
 * @date 2023-10-24 13:42:24 Tuesday
 */
@RestController
public class HelloController {

    @GetMapping("/log")
    public String helloLog() {

        return "hello log： " + LocalDateTime.now();
    }
}
