package com.dhn.sentinel.dhnsentinel.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class StopController {

    @GetMapping("/stop")
    public void stop() {
        log.info("stop로직 만들예정");
    }
}
