package com.dhn.sentinel.dhnsentinel.controller;

import com.dhn.sentinel.dhnsentinel.service.LocalStartService;
import com.dhn.sentinel.dhnsentinel.service.SentinelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Sentinel {

    @Autowired
    private SentinelService sentinelService;

    @Autowired
    private LocalStartService localStartService;

    /*
    @Scheduled(cron = "0/30 * * * * * ")
    private void sentinel() {

        // 서버 접근 체크 (살아있는지)
        boolean status = sentinelService.Accsess();

        if(status){
            // DB 체크
            sentinelService.DatabaseCheck();

            // DHN 체크
            sentinelService.DHNAgentCheck("DHNCenter");
            sentinelService.DHNAgentCheck("DHNServer");

            // NANO 체크
            sentinelService.NanoAgentCheck("nanoagent");

            //LG 체크
            sentinelService.lgAgentCheck("lguplus");

        }else{
            // 로컬 DB 실행
            localStartService.startDatabase();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }

            // 로컬 NANO 실행
            localStartService.startNanoAgent("nanoagent");
            // 로컬 LG 실행
            localStartService.startLGAgent("lguplus");

            // 로컬 DHN 실행
            localStartService.startDHNCenter();
            localStartService.startDHNServer();

        }

    }
     */

    @Scheduled(cron = "0/10 * * * * * ")
    private void Test() {
        localStartService.test();
    }
}
