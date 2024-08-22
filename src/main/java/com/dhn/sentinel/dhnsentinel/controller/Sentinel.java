package com.dhn.sentinel.dhnsentinel.controller;

import com.dhn.sentinel.dhnsentinel.service.LocalStartService;
import com.dhn.sentinel.dhnsentinel.service.SendAlimtalk;
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

    private boolean isStart = true;

    @Scheduled(cron = "0/30 * * * * * ")
    private void sentinel() {

        if(isStart){
            // 서버 접근 체크 (살아있는지)
            boolean status = sentinelService.Accsess();

            if(status){
                // DB 체크
                boolean dbcheck = sentinelService.DatabaseCheck();
                if(!dbcheck){
                    isStart = false;
                    return;
                }

                // DHN 체크
                boolean dhncenter = sentinelService.DHNAgentCheck("DHNCenter");
                if(!dhncenter){
                    isStart = false;
                    return;
                }
                boolean dhnserver = sentinelService.DHNAgentCheck("DHNServer");
                if(!dhnserver){
                    isStart = false;
                    return;
                }

                // NANO 체크
                boolean nano = sentinelService.NanoAgentCheck("nanoagent");
                if(!nano){
                    isStart = false;
                    return;
                }

                //LG 체크
                boolean lg = sentinelService.lgAgentCheck("lguplus");
                if(!lg){
                    isStart = false;
                    return;
                }

            }else{
                // 로컬 실행
                localStartService.allStart();
                isStart = false;
            }
        }
    }



}
