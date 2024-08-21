package com.dhn.sentinel.dhnsentinel.service;

import com.dhn.sentinel.dhnsentinel.vo.AlimtalkVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SendAlimtalk {

    @Value("${alimtalk.url}")
    private String aturl;
    @Value("${alimtalk.temp}")
    private String temp;
    @Value("${dhn.server}")
    private String server;
    @Value("${alimtalk.key}")
    private String key;
    @Value("${alimtalk.phn}")
    private List<String> phn;
    @Value("${alimtalk.k2nd}")
    private String k2nd;

    public void send(String company, String issue, String issue2, String status) {
        List<AlimtalkVO> list = new ArrayList<>();

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateFormat.format(date);


        for (String phone : phn) {
            AlimtalkVO alimtalkVO = new AlimtalkVO();
            alimtalkVO.setTmp_number(temp);
            alimtalkVO.setKakao_phone(phone);
            alimtalkVO.setKakao_2nd(k2nd);
            alimtalkVO.setKakao_add1(company);
            alimtalkVO.setKakao_add2(time);
            alimtalkVO.setKakao_add3(server);
            alimtalkVO.setKakao_add4(issue);
            alimtalkVO.setKakao_add5(issue2);
            alimtalkVO.setKakao_add6(status);
            alimtalkVO.setMst_type1("at");
            list.add(alimtalkVO);
        }

        log.info(list.toString());

        List<String> apiList = new ArrayList<>();
        ObjectMapper mp = new ObjectMapper();

        try{
            for (AlimtalkVO alimtalkVO : list) {
                apiList.add(mp.writeValueAsString(alimtalkVO));
            }
        }catch (JsonProcessingException e){
            log.error(e.getMessage());
        }
        log.info(apiList.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", key);

        RestTemplate rst = new RestTemplate();

        for (String api : apiList) {
            HttpEntity<String> entity = new HttpEntity<>(api.toString(),headers);
            ResponseEntity<String> response = null;

            try{
                response = rst.postForEntity(aturl,entity,String.class);
                log.info(response.getStatusCode() + " / " + response.getBody());

                if(response.getStatusCode() == HttpStatus.OK){
                    log.info("메세지 전송 완료 : " + apiList.size() + "건 / " + time);
                }else{
                    Map<String, String> res = mp.readValue(response.getBody().toString(), Map.class);
                    log.info("메세지 전송오류 : " + res.get("message") + " / " + time);
                }

            }catch (Exception e){
                log.error("API 전송 오류 : "+e.getMessage());
            }
        }


    }
}
