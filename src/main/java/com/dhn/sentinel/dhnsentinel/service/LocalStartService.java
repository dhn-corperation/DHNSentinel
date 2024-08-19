package com.dhn.sentinel.dhnsentinel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LocalStartService {

    @Value("${dhn.username}")
    private String username;
    @Value("${dhn.host}")
    private String host;
    @Value("${dhn.port}")
    private int port;
    @Value("${dhn.password}")
    private String password;

    @Value("${database.user}")
    private String dbuser;
    @Value("${database.password}")
    private String dbpw;
    @Value("${database.port}")
    private String dbport;

    @Value("${dhncenter.port}")
    private String cPort;
    @Value("${dhnserver.port}")
    private String sPort;

    // 로컬 명령어 실행 메서드
    public void executeLocalCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("명령어 실행 성공: " + command);
            } else {
                log.error("명령어 실행 실패. 종료 코드: " + exitCode);
            }

        } catch (Exception e) {
            log.error("로컬 명령어 실행 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // 로컬에서 DB 실행
    public void startDatabase() {
        String command = "/etc/init.d/mysqld start";
        executeLocalCommand(command);
    }

    // 로컬에서 DHNCenter 실행
    public void startDHNCenter() {
        String command = "systemctl start DHNCenter";
        executeLocalCommand(command);
    }

    // 로컬에서 DHNServer 실행
    public void startDHNServer() {
        String command = "systemctl start DHNServer";
        executeLocalCommand(command);
    }

    // 로컬에서 NanoAgent 실행
    public void startNanoAgent(String service) {
        String command = "/root/"+service+"/manager-linux.sh start";
        executeLocalCommand(command);
    }

    // 로컬에서 LGU+ 에이전트 실행
    public void startLGAgent(String service) {
        String command = "/root/"+service+"/bin/uagent.sh start";
        executeLocalCommand(command);
    }
}
