package com.dhn.sentinel.dhnsentinel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LocalStartService {

    @Value("${dhn.company}")
    private String company;
    @Value("${dhn.server}")
    private String server;
    @Value("${dhn.localserver}")
    private String localserver;

    @Autowired
    private SendAlimtalk sendAlimtalk;

    // 로컬 명령어 실행 메서드
    public boolean executeLocalCommand(String command, String issue) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("명령어 실행 성공: " + command);
            } else {
                log.error("명령어 실행 실패. 종료 코드: " + exitCode);
                sendAlimtalk.send(company ,localserver, issue, "실행 명령 실패");
                return false;
            }

        } catch (Exception e) {
            log.error("로컬 명령어 실행 중 오류 발생: " + e.getMessage(), e);
            sendAlimtalk.send(company ,localserver, issue, "실행 중 오류발생");
            return false;
        }
        return true;
    }

    public void allStart(){
        // 로컬 DB 실행
        startDatabase();
        boolean db = true;
        boolean nano = true;
        boolean lg = true;
        boolean dhnCenter = true;
        boolean dhnServer = true;
        try {
            // 로컬 DB 실행
            db = startDatabase();
            if(!db){
                return;
            }
            Thread.sleep(5000);

            // 로컬 NANO 실행
            nano = startNanoAgent("nanoagent");
            if(!nano){
                return;
            }
            Thread.sleep(1000);
            // 로컬 LG 실행
            lg = startLGAgent("lguplus");
            if(!lg){
                return;
            }
            Thread.sleep(1000);

            // 로컬 DHN 실행
            dhnCenter = startDHNCenter();
            if(!dhnCenter){
                return;
            }
            dhnServer = startDHNServer();
            if(!dhnServer){
                return;
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        sendAlimtalk.send(company,server,localserver+"서버","실행");

    }

    // 로컬에서 DB 실행
    public boolean startDatabase() {
        String command = "/etc/init.d/mysqld start";
        return executeLocalCommand(command,"DB");
    }

    // 로컬에서 DHNCenter 실행
    public boolean startDHNCenter() {
        String command = "systemctl start DHNCenter";
        return executeLocalCommand(command,"DHNCenter");
    }

    // 로컬에서 DHNServer 실행
    public boolean startDHNServer() {
        String command = "systemctl start DHNServer";
        return executeLocalCommand(command,"DHNServer");
    }

    // 로컬에서 NanoAgent 실행
    public boolean startNanoAgent(String service) {
        String command = "/root/"+service+"/manager-linux.sh start";
        return executeLocalCommand(command,"NANOAgent");
    }

    // 로컬에서 LGU+ 에이전트 실행
    public boolean startLGAgent(String service) {
        String command = "/root/"+service+"/bin/uagent.sh start";
        return executeLocalCommand(command,"LGAgent");
    }

}
