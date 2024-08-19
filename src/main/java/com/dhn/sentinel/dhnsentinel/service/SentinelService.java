package com.dhn.sentinel.dhnsentinel.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;

@Service
@Slf4j
public class SentinelService {

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

    public boolean Accsess(){
        try {
            InetAddress inet = InetAddress.getByName(host);
            if (inet.isReachable(5000)) { // 5초 동안 응답 대기
                log.info("서버는 정상적으로 연결되었습니다.");
            } else {
                log.info("서버에 연결할 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    // DB 체크
    public void DatabaseCheck(){
        String url = "jdbc:mariadb://" + host + ":" + dbport + "?connectTimeout=5000";
        try(Connection connection = DriverManager.getConnection(url,dbuser,dbpw)){
            if (connection != null && !connection.isClosed()) {
                log.info("데이터베이스 연결 성공! DB가 작동 중입니다.");
            }
        }catch (Exception e){
            log.info("데이터베이스에 연결할 수 없습니다. 재가동을 시작합니다.");
            DatabaseStart();
        }
    }

    private Session sessionConnect() throws Exception{
        JSch jsch = new JSch();

        Session session = jsch.getSession(username, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();
        return session;
    }

    // DB 실행 명령어 및 세션 커넥트
    private void DatabaseStart(){
        try{
            Session session = sessionConnect();
            String command = "/etc/init.d/mysqld start";
            executeRemoteCommand(session, command);

            String url = "jdbc:mariadb://" + host + ":" + dbport + "?connectTimeout=5000";
            try(Connection connection = DriverManager.getConnection(url,dbuser,dbpw)){
                if (connection != null && !connection.isClosed()) {
                    log.info("Database 정상 실행 되었습니다.");
                }
            }catch (Exception e){
                log.error("데이터베이스 실행 실패하였습니다.", e);
            }

            session.disconnect();
        }catch (Exception e){
            log.error("명령어 실행 중 예외가 발생했습니다.", e);
        }
    }

    // DB 실행 로직
    private void executeRemoteCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        channel.connect();

        Thread.sleep(2000);
        channel.disconnect();
    }

    // Center, Server 체크
    public void DHNAgentCheck(String service){

        String port = service.equals("DHNCenter")?cPort:sPort;

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate rt = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<String>(header);

        try{
            ResponseEntity<String> response = rt.exchange("http://"+host+":"+port, HttpMethod.GET,entity,String.class);

            if(response.getStatusCode() == HttpStatus.OK){
                log.info(service+" 정상 작동 중입니다.");
            }else{
                log.info(service+" 작동이 멈췄습니다. 재기동 시작합니다.");
                Session session = sessionConnect();
                String command = "systemctl start "+service;

                executeRemoteCommand(session,command);

                response = rt.exchange("http://"+host+":"+port, HttpMethod.GET,entity,String.class);
                if(response.getStatusCode() == HttpStatus.OK){
                    log.info(service+" 재가동 완료 되었습니다.");
                }else{
                    log.info(service+" 재가동 실패하였습니다. 확인바랍니다.");
                }
            }

        }catch (HttpClientErrorException e) {
            log.error("HTTP 통신 오류 : " + e.getStatusCode() + ", " + e.toString());
        }catch (Exception e){
            log.error("기타 오류 : " + e.toString());
        }
    }

    // Nano 체크
    public void NanoAgentCheck(String service){
        try {
            Session session = sessionConnect();

            String command = "/root/"+service+"/manager-linux.sh status";
            boolean isRunning = nanoRemodeCommand(session,command);

            if(!isRunning){
                log.info("{} 에이전트가 동작하지 않습니다. 재실행을 시도합니다.",service);

                String startCommand = "/root/"+service+"/manager-linux.sh start";
                executeRemoteCommand(session,startCommand);

                Thread.sleep(1000);
                boolean reRunning = nanoRemodeCommand(session,command);

                if(!reRunning){
                    log.info("{} 에이전트 재가동 실패 확인 바랍니다.",service);
                }else{
                    log.info("{} 에이전트 재가동 되었습니다.",service);
                }
            }else{
                log.info("{} 에이전트가 정상 작동 중입니다.",service);
            }

            session.disconnect();
        }catch (Exception e){
            log.error("NANO 체크중 오류 발생 : " + e.getMessage(), e);
        }
    }

    // 나노 상태 체크
    private boolean nanoRemodeCommand(Session session, String command) throws Exception {

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()))) {
            channel.connect();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("is running")) {
                    return true;
                } else if (line.contains("is not running")) {
                    return false;
                }
            }
        } finally {
            channel.disconnect();
        }
        return false;
    }

    // lg 체크
    public void lgAgentCheck(String service){
        try {
            Session session = sessionConnect();

            String command = "/root/"+service+"/bin/uagent.sh start";
            boolean isRunning = lgRemodeCommand(session,command);

            if(!isRunning){
                log.info("{} 에이전트가 동작하지 않습니다. 재실행을 시도합니다.",service);

                boolean reRunning = lgRemodeCommand(session,command);

                if(!reRunning){
                    log.info("{} 에이전트 재가동 실패 확인 바랍니다.",service);
                }else{
                    log.info("{} 에이전트 재가동 되었습니다.",service);
                }
            }else{
                log.info("{} 에이전트가 정상 작동 중입니다.",service);
            }

            session.disconnect();
        }catch (Exception e){
            log.error("LG 체크중 오류 발생 : " + e.getMessage(), e);
        }
    }

    // LG 상태 체크
    private boolean lgRemodeCommand(Session session, String command) throws Exception {

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()))) {
            channel.connect();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("is already running.")) {
                    return true;
                }
            }
        } finally {
            channel.disconnect();
        }
        return false;
    }

}
