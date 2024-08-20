package com.dhn.sentinel.dhnsentinel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SendAlimtalk {

    @Value("${alimtalk.url}")
    private String aturl;

    @Value("${alimtalk.temp}")
    private String temp;

    @Value("${alimtalk.server}")
    private String server;

    @Value("${alimtalk.profile}")
    private String profile;

    @Value("${alimtalk.phn}")
    private List<String> phn;


    public void test() {
        log.info("test");
        log.info(aturl);
        log.info(temp);
        log.info(server);
        log.info(profile);
        log.info(phn.toString());
    }
}
